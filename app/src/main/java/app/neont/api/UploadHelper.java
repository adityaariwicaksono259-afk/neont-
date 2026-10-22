package app.neont.api;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * UploadHelper — upload file ke Filebase via javin-cf Pages Function.
 *
 * Endpoint: https://neont-servers.pages.dev/api/upload
 * Method  : POST multipart/form-data
 * Field   : file (File), folder ("avatar" | "chat")
 * Return  : { ok: true, url: "https://s3.filebase.io/neont-media/neont/avatar/..." }
 *
 * Batasan:
 * - Max 10 MB (dari server)
 * - Tipe diizinkan: image/jpeg, image/png, image/webp, image/gif,
 *                   video/mp4, video/webm, audio/mpeg, audio/ogg
 *
 * Semua callback dijalankan di main thread.
 */
public class UploadHelper {

    private static final String UPLOAD_URL = "https://neont-servers.pages.dev/api/upload";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** Ukuran file max 10 MB (sesuai server). */
    public static final long MAX_SIZE = 10L * 1024 * 1024;

    public interface UploadCallback {
        /** Dipanggil saat upload sukses. url = URL publik Filebase. */
        void onSuccess(String publicUrl);
        /** Dipanggil saat gagal (network, server error, tipe file, dll). */
        void onError(String message);
    }

    // ===== API publik =====

    /** Upload foto profil. Folder = "avatar". */
    public static void uploadAvatar(File file, UploadCallback cb) {
        upload(file, "avatar", cb);
    }

    /** Upload media chat (gambar/video/audio). Folder = "chat". */
    public static void uploadChatMedia(File file, UploadCallback cb) {
        upload(file, "chat", cb);
    }

    /**
     * Upload umum. folder dibatasi server (regex [a-z0-9_-], max 20 char).
     */
    public static void upload(File file, String folder, UploadCallback cb) {
        if (file == null || !file.exists()) {
            cb.onError("File tidak ada");
            return;
        }
        if (file.length() > MAX_SIZE) {
            cb.onError("File terlalu besar (max 10 MB)");
            return;
        }
        if (!file.canRead()) {
            cb.onError("File tidak bisa dibaca (permission?)");
            return;
        }

        String safeFolder = (folder == null) ? "misc" : folder.replaceAll("[^a-z0-9_-]", "");
        if (safeFolder.isEmpty()) safeFolder = "misc";

        // Tentukan MIME type dari ekstensi
        String mime = guessMime(file.getName());
        MediaType fileType = MediaType.parse(mime != null ? mime : "application/octet-stream");

        RequestBody fileBody = RequestBody.create(file, fileType);
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .addFormDataPart("folder", safeFolder)
                .build();

        Request req = new Request.Builder()
                .url(UPLOAD_URL)
                .post(body)
                .header("Accept", "application/json")
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> cb.onError("Network error: " + e.getMessage()));
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response res) throws IOException {
                final String bodyStr = res.body() != null ? res.body().string() : "";
                final int code = res.code();
                mainHandler.post(() -> {
                    try {
                        JSONObject json = new JSONObject(bodyStr);
                        if (json.optBoolean("ok") && json.has("url")) {
                            cb.onSuccess(json.getString("url"));
                        } else {
                            String msg = json.optString("message", "Upload gagal");
                            if (json.has("detail")) msg += " — " + json.optString("detail");
                            cb.onError(msg + " (HTTP " + code + ")");
                        }
                    } catch (Exception e) {
                        cb.onError("Parse error (HTTP " + code + "): " + e.getMessage());
                    }
                });
            }
        });
    }

    // ===== Util =====

    /** Tebak MIME type dari ekstensi file. */
    private static String guessMime(String fileName) {
        if (fileName == null) return null;
        String name = fileName.toLowerCase();
        int dot = name.lastIndexOf('.');
        if (dot < 0) return null;
        String ext = name.substring(dot + 1);

        switch (ext) {
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png":  return "image/png";
            case "webp": return "image/webp";
            case "gif":  return "image/gif";
            case "mp4":  return "video/mp4";
            case "webm": return "video/webm";
            case "mp3":  return "audio/mpeg";
            case "ogg":  return "audio/ogg";
            default: return null;
        }
    }
}
