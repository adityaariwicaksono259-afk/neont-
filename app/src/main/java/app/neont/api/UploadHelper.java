package app.neont.api;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

public class UploadHelper {

    private static final String UPLOAD_URL = "https://jvin.pages.dev/api/chat/upload";

    public interface UploadCallback {
        void onSuccess(String fileId);
        void onFailure(String error);
    }

    public static void uploadFile(Context context, Uri fileUri, String type, UploadCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // 1. Copy file dari Uri ke Cache (tanpa kompres)
                InputStream is = context.getContentResolver().openInputStream(fileUri);
                File fileToUpload = new File(context.getCacheDir(), "upload_" + System.currentTimeMillis() + (type.equals("image") ? ".jpg" : ".mp4"));
                FileOutputStream fos = new FileOutputStream(fileToUpload);
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) fos.write(buffer, 0, read);
                fos.close();
                is.close();

                // 2. Kirim ke javin-cf (multipart)
                String boundary = "*****" + System.currentTimeMillis() + "*****";
                URL url = new URL(UPLOAD_URL + "?room_id=0");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                OutputStream os = conn.getOutputStream();
                os.write(("--" + boundary + "\r\n").getBytes());
                os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileToUpload.getName() + "\"\r\n").getBytes());
                os.write(("Content-Type: " + (type.equals("image") ? "image/jpeg" : "video/mp4") + "\r\n\r\n").getBytes());

                FileInputStream fis = new FileInputStream(fileToUpload);
                byte[] buf = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buf)) != -1) os.write(buf, 0, bytesRead);
                fis.close();
                
                os.write(("\r\n--" + boundary + "--\r\n").getBytes());
                os.flush();
                os.close();

                // 3. Baca Response
                InputStream is2 = conn.getInputStream();
                java.util.Scanner s = new java.util.Scanner(is2).useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";
                is2.close();

                JSONObject json = new JSONObject(response);
                if (json.has("ok") && json.getBoolean("ok")) {
                    String mediaUrl = json.optJSONObject("message") != null ? json.getJSONObject("message").optString("media_url", null) : null;
                    if (mediaUrl != null) callback.onSuccess(mediaUrl);
                    else callback.onFailure("File ID nggak ketemu");
                } else {
                    callback.onFailure(json.optString("message", "Upload gagal"));
                }

            } catch (Exception e) {
                callback.onFailure(e.getMessage());
            }
        });
    }
}
