package app.neont.api;

import com.google.firebase.auth.FirebaseAuthException;

public class AuthHelper {
    public static String getErrorMessage(Exception e) {
        String errorCode = "";
        if (e instanceof FirebaseAuthException) {
            errorCode = ((FirebaseAuthException) e).getErrorCode();
        }

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
            default:
                return "Gagal: " + e.getMessage();
        }
    }
}
