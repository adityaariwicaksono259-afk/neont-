package app.neont.api;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.iceteck.silicompressor.SiliCompressor;
import org.json.JSONObject;

public class UploadHelper {

    // GANTI PAKE URL CLOUDFLARE WORKER LO
    private static final String UPLOAD_URL = "https://jvin.pages.dev/api/chat/upload";

    public interface UploadCallback {
        void onSuccess(String fileId);
        void onFailure(String error);
    }

    public static void compressAndUpload(Context context, Uri fileUri, String type, UploadCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // 1. Kompres File
                File compressedFile;
                if ("image".equals(type)) {
                    compressedFile = SiliCompressor.with(context).compressVideo(fileUri, context.getCacheDir().getPath());
                } else {
                    compressedFile = SiliCompressor.with(context).compressVideo(fileUri, context.getCacheDir().getPath());
                }

                // 2. Kirim ke javin-cf (multipart/form-data)
                String boundary = "*****" + System.currentTimeMillis() + "*****";
                URL url = new URL(UPLOAD_URL + "?room_id=0"); // room_id 0 biar nggak masuk chat
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                OutputStream os = conn.getOutputStream();
                os.write(("--" + boundary + "\r\n").getBytes());
                os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + compressedFile.getName() + "\"\r\n").getBytes());
                os.write(("Content-Type: " + (type.equals("image") ? "image/jpeg" : "video/mp4") + "\r\n\r\n").getBytes());
                
                FileInputStream fis = new FileInputStream(compressedFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                fis.close();
                
                os.write(("\r\n--" + boundary + "--\r\n").getBytes());
                os.flush();
                os.close();

                // 3. Baca Response
                InputStream is = conn.getInputStream();
                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";
                is.close();

                // 4. Parse JSON (cari field media_url atau file_id)
                JSONObject json = new JSONObject(response);
                if (json.has("ok") && json.getBoolean("ok")) {
                    // Sesuaikan sama response javin-cf lo
                    String mediaUrl = null;
                    if (json.has("message")) {
                        mediaUrl = json.getJSONObject("message").optString("media_url", null);
                    }
                    if (mediaUrl != null) {
                        callback.onSuccess(mediaUrl);
                    } else {
                        callback.onFailure("File ID nggak ketemu di response");
                    }
                } else {
                    callback.onFailure(json.optString("message", "Upload gagal"));
                }

            } catch (Exception e) {
                callback.onFailure(e.getMessage());
            }
        });
    }
}
