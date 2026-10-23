package app.neont.api;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * D1Helper — jembatan ke Cloudflare D1 via javin-cf Pages Functions.
 *
 * Endpoint: https://neont-servers.pages.dev/api/user
 * - POST   { firebase_uid, display_name }              → create user (auto-generate user_code)
 * - GET    ?uid=<firebase_uid>  atau  ?code=<user_code> → get user
 * - PUT    { firebase_uid, username?, display_name?, bio?, photo_url? } → update
 *
 * Semua callback dijalankan di main thread.
 */
public class D1Helper {

    public static final String BASE_URL = "https://neont-servers.pages.dev";
    private static final String NEONT_SECRET = "neont_rahasia_123";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface UserCallback {
        void onSuccess(JSONObject user);
        void onError(String message);
    }

    /**
     * Create user di D1. Idempotent: kalau sudah ada, balas data existing (existed=true).
     * Dipanggil setelah Firebase Auth sukses (register atau login pertama).
     */
    public static void createUser(String firebaseUid, String displayName, UserCallback cb) {
        if (firebaseUid == null || firebaseUid.isEmpty()) {
            cb.onError("firebaseUid kosong");
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("firebase_uid", firebaseUid);
            body.put("display_name", displayName == null ? "" : displayName);

            Request req = new Request.Builder()
                    .url(BASE_URL + "/api/user")
                    .post(RequestBody.create(body.toString(), JSON_TYPE))
                    .header("Accept", "application/json")
                    .header("X-Neont-Secret", NEONT_SECRET)
                    .build();

            enqueue(req, cb);
        } catch (Exception e) {
            cb.onError("createUser exception: " + e.getMessage());
        }
    }

    /** Get user berdasarkan Firebase UID. */
    public static void getUser(String firebaseUid, UserCallback cb) {
        if (cb == null) return; // Cegah NPE
        if (firebaseUid == null || firebaseUid.isEmpty()) {
            cb.onError("firebaseUid kosong");
            return;
        }
        Request req = new Request.Builder()
                .url(BASE_URL + "/api/user?uid=" + firebaseUid)
                .get()
                .header("Accept", "application/json")
                    .header("X-Neont-Secret", NEONT_SECRET)
                .build();
        enqueue(req, cb);
    }

    /** Get user berdasarkan user_code (NT-XXXXXX). */
    public static void getUserByCode(String userCode, UserCallback cb) {
        if (userCode == null || userCode.isEmpty()) {
            cb.onError("userCode kosong");
            return;
        }
        Request req = new Request.Builder()
                .url(BASE_URL + "/api/user?code=" + userCode)
                .get()
                .header("Accept", "application/json")
                    .header("X-Neont-Secret", NEONT_SECRET)
                .build();
        enqueue(req, cb);
    }

    /**
     * Update profil user. Field yang boleh diisi: username, display_name, bio, photo_url.
     * Contoh pemanggilan:
     *   JSONObject f = new JSONObject();
     *   f.put("display_name", "Nama Baru");
     *   f.put("bio", "Halo dunia");
     *   D1Helper.updateUser(uid, f, cb);
     */
    public static void updateUser(String firebaseUid, JSONObject fields, UserCallback cb) {
        if (firebaseUid == null || firebaseUid.isEmpty()) {
            cb.onError("firebaseUid kosong");
            return;
        }
        if (fields == null) fields = new JSONObject();
        try {
            fields.put("firebase_uid", firebaseUid);

            Request req = new Request.Builder()
                    .url(BASE_URL + "/api/user")
                    .put(RequestBody.create(fields.toString(), JSON_TYPE))
                    .header("Accept", "application/json")
                    .header("X-Neont-Secret", NEONT_SECRET)
                    .build();

            enqueue(req, cb);
        } catch (Exception e) {
            cb.onError("updateUser exception: " + e.getMessage());
        }
    }

    // ===== Internal =====

    private static void enqueue(Request req, UserCallback cb) {
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> {
                    if (cb != null) cb.onError("Network error: " + e.getMessage());
                });
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response res) throws IOException {
                final String bodyStr = res.body() != null ? res.body().string() : "";
                final int code = res.code();
                mainHandler.post(() -> {
                    try {
                        JSONObject json = new JSONObject(bodyStr);

                        if (json.optBoolean("ok") && json.has("user")) {
                            cb.onSuccess(json.getJSONObject("user"));
                            return;
                        }

                        // Kalau 404 saat GET — user belum ada, bukan error fatal
                        if (code == 404) {
                            cb.onError("USER_NOT_FOUND");
                            return;
                        }

                        cb.onError(json.optString("message", "HTTP " + code));
                    } catch (Exception e) {
                        cb.onError("Parse error (HTTP " + code + "): " + e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * Catat login terakhir ke D1 (last_login_at + last_device_id).
     * Dipanggil setelah login sukses. Tidak blocking.
     */
    public static void recordLastLogin(String firebaseUid, String deviceId, UserCallback cb) {
        if (firebaseUid == null || firebaseUid.isEmpty()) {
            if (cb != null) cb.onError("firebaseUid kosong");
            return;
        }
        if (cb == null) {
            // Jika cb null, kita buat callback dummy agar tidak crash
            cb = new UserCallback() {
                @Override public void onSuccess(org.json.JSONObject user) {}
                @Override public void onError(String message) {}
            };
        }
        try {
            JSONObject body = new JSONObject();
            body.put("firebase_uid", firebaseUid);
            body.put("last_login_at", System.currentTimeMillis());
            body.put("last_device_id", deviceId == null ? "" : deviceId);
            updateUser(firebaseUid, body, cb);
        } catch (Exception e) {
            if (cb != null) cb.onError("recordLastLogin exception: " + e.getMessage());
        }
    }

    public interface SearchCallback {
        void onSuccess(org.json.JSONArray users);
        void onError(String message);
    }

    public static void searchUser(String query, SearchCallback cb) {
        if (query == null || query.trim().length() < 5) {
            cb.onError("Minimal 5 karakter");
            return;
        }
        String q = query.trim().replace("@", "");
        Request req = new Request.Builder()
                .url(BASE_URL + "/api/search?q=" + android.net.Uri.encode(q))
                .get()
                .header("Accept", "application/json")
                    .header("X-Neont-Secret", NEONT_SECRET)
                .build();

        client.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> cb.onError("Network error: " + e.getMessage()));
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response res) throws IOException {
                final String bodyStr = res.body() != null ? res.body().string() : "";
                final int code = res.code();
                mainHandler.post(() -> {
                    try {
                        JSONObject json = new JSONObject(bodyStr);
                        if (json.optBoolean("ok") && json.has("users")) {
                            cb.onSuccess(json.getJSONArray("users"));
                        } else {
                            cb.onError(json.optString("message", "HTTP " + code));
                        }
                    } catch (Exception e) {
                        cb.onError("Parse error: " + e.getMessage());
                    }
                });
            }
        });
    }
}
