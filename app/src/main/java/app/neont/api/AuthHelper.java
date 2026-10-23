package app.neont.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import org.json.JSONObject;

/**
 * AuthHelper — logika login/register terpusat.
 *
 * Fitur keamanan:
 * - Validasi email (regex Patterns.EMAIL_ADDRESS).
 * - Validasi password (min 8, harus ada huruf & angka).
 * - Anti brute-force: 5x gagal → kunci 30 detik (per email, disimpan di SharedPreferences).
 *
 * Integrasi:
 * - Register → Firebase Auth → D1Helper.createUser() (tabel neont_users).
 * - Login    → Firebase Auth → D1Helper.getUser() (sinkronisasi data user).
 */
public class AuthHelper {

    private static final String PREFS = "neont_auth";
    private static final String KEY_FAIL_COUNT = "fail_count_";
    private static final String KEY_LOCK_UNTIL = "lock_until_";
    private static final int MAX_FAILED = 3;
    private static final long LOCK_MS = 5 * 60_000L; // 5 menit

    // ===== Callback =====

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String message);
    }

    // ===== Validasi =====

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Password: min 8 karakter, minimal 1 huruf + 1 angka.
     * Return null jika valid, atau pesan error jika tidak.
     */
    public static String validatePassword(String pass) {
        if (pass == null || pass.length() < 8) return "Password minimal 8 karakter.";
        boolean hasLetter = false, hasDigit = false;
        for (char c : pass.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasLetter || !hasDigit) return "Password harus mengandung huruf & angka.";
        return null;
    }

    // ===== Anti brute-force =====

    /** Cek apakah email ini sedang terkunci. Return sisa detik (0 = tidak terkunci). */
    public static long getRemainingLockSeconds(Context ctx, String email) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long until = sp.getLong(KEY_LOCK_UNTIL + safeKey(email), 0L);
        long now = System.currentTimeMillis();
        if (until > now) return (until - now + 999) / 1000;
        return 0;
    }

    private static void recordFailure(Context ctx, String email) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String k = safeKey(email);
        int count = sp.getInt(KEY_FAIL_COUNT + k, 0) + 1;
        if (count >= MAX_FAILED) {
            sp.edit()
                    .putInt(KEY_FAIL_COUNT + k, 0)
                    .putLong(KEY_LOCK_UNTIL + k, System.currentTimeMillis() + LOCK_MS)
                    .apply();
        } else {
            sp.edit().putInt(KEY_FAIL_COUNT + k, count).apply();
        }
    }

    private static void clearFailure(Context ctx, String email) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String k = safeKey(email);
        sp.edit()
                .remove(KEY_FAIL_COUNT + k)
                .remove(KEY_LOCK_UNTIL + k)
                .apply();
    }

    private static String safeKey(String email) {
        return email == null ? "unknown" : email.toLowerCase().replaceAll("[^a-z0-9@.]", "_");
    }

    // ===== Login =====

    public static void login(Context ctx, String email, String password, AuthCallback cb) {
        if (!isValidEmail(email)) {
            cb.onError("Format email tidak valid.");
            return;
        }
        if (password == null || password.isEmpty()) {
            cb.onError("Password wajib diisi.");
            return;
        }

        long lock = getRemainingLockSeconds(ctx, email);
        if (lock > 0) {
            cb.onError("Terlalu banyak percobaan. Tunggu " + lock + " detik.");
            return;
        }

        final Context appCtx = ctx.getApplicationContext();

        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(a -> {
                    clearFailure(appCtx, email);
                    FirebaseUser user = a.getUser();
                    if (user == null) {
                        cb.onError("Login sukses tapi user null.");
                        return;
                    }
                    // Pastikan UID tidak null
                    if (user.getUid() == null) {
                        cb.onError("UID kosong.");
                        return;
                    }
                    // Sinkronisasi ke D1 (create jika belum ada, idempotent)
                    D1Helper.createUser(user.getUid(), safeName(user), new D1Helper.UserCallback() {
                        @Override public void onSuccess(org.json.JSONObject u) {
                            // Catat login terakhir (async, tidak blocking)
                            D1Helper.recordLastLogin(user.getUid(), getDeviceId(appCtx), null);
                            cb.onSuccess(user);
                        }
                        @Override public void onError(String message) {
                            // D1 gagal tidak boleh menggagalkan login (Firebase sudah sukses)
                            cb.onSuccess(user);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    recordFailure(appCtx, email);
                    cb.onError(getErrorMessage(e));
                });
    }

    // ===== Register =====

    public static void register(Context ctx, String email, String password, AuthCallback cb) {
        if (!isValidEmail(email)) {
            cb.onError("Format email tidak valid.");
            return;
        }
        String passErr = validatePassword(password);
        if (passErr != null) {
            cb.onError(passErr);
            return;
        }

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(a -> {
                    FirebaseUser user = a.getUser();
                    if (user == null) {
                        cb.onError("Register sukses tapi user null.");
                        return;
                    }
                    // Set displayName default (sebelum user isi username)
                    String defaultName = email.split("@")[0];
                    UserProfileChangeRequest prof = new UserProfileChangeRequest.Builder()
                            .setDisplayName(defaultName)
                            .build();
                    user.updateProfile(prof);

                    // Buat row di D1
                    D1Helper.createUser(user.getUid(), defaultName, new D1Helper.UserCallback() {
                        @Override public void onSuccess(org.json.JSONObject u) {
                            cb.onSuccess(user);
                        }
                        @Override public void onError(String message) {
                            // Firebase sudah sukses; D1 bisa disinkron nanti via login
                            cb.onSuccess(user);
                        }
                    });
                })
                .addOnFailureListener(e -> cb.onError(getErrorMessage(e)));
    }

    // ===== Util =====

    private static String safeName(FirebaseUser user) {
        String n = user.getDisplayName();
        if (n == null || n.isEmpty()) {
            String email = user.getEmail();
            if (email != null && email.contains("@")) n = email.split("@")[0];
            else n = "User";
        }
        return n;
    }

    /** Terjemah error Firebase ke pesan Indonesia (fungsi lama, dipertahankan). */
    public static String getErrorMessage(@NonNull Exception e) {
        String errorCode = "";
        if (e instanceof FirebaseAuthException) {
            errorCode = ((FirebaseAuthException) e).getErrorCode();
        }
        if (errorCode == null) errorCode = "";

        switch (errorCode) {
            case "ERROR_INVALID_EMAIL":
                return "Format email salah.";
            case "ERROR_USER_NOT_FOUND":
                return "Email belum terdaftar. Silakan daftar dulu.";
            case "ERROR_WRONG_PASSWORD":
                return "Password salah. Coba lagi.";
            case "ERROR_EMAIL_ALREADY_IN_USE":
                return "Email sudah terdaftar. Silakan masuk.";
            case "ERROR_WEAK_PASSWORD":
                return "Password terlalu lemah (minimal 8 karakter).";
            case "ERROR_TOO_MANY_REQUESTS":
                return "Terlalu banyak percobaan. Tunggu sebentar.";
            case "ERROR_NETWORK_REQUEST_FAILED":
                return "Koneksi internet bermasalah.";
            case "ERROR_USER_DISABLED":
                return "Akun dinonaktifkan.";
            case "ERROR_OPERATION_NOT_ALLOWED":
                return "Metode login tidak diizinkan.";
            default:
                return "Gagal: " + e.getMessage();
        }
    }

    /** Device ID sederhana (ANDROID_ID). Untuk Hard Device nanti. */
    private static String getDeviceId(Context ctx) {
        try {
            return android.provider.Settings.Secure.getString(
                    ctx.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            return "";
        }
    }
}
