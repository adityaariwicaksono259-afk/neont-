package app.neont.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;
import java.io.File;
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

    public static void compressAndUpload(Context context, Uri fileUri, String type, UploadCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                File fileToUpload;

                if ("image".equals(type)) {
                    // KOMPRES FOTO (Tanpa Library)
                    InputStream is = context.getContentResolver().openInputStream(fileUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    
                    if (bitmap == null) throw new Exception("Gagal baca gambar");

                    // Scale down biar ringan (max 1080px)
                    int maxSize = 1080;
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    if (width > maxSize || height > maxSize) {
                        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
                        width = Math.round(ratio * width);
                        height = Math.round(ratio * height);
                        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                    }

                    File compressed = new File(context.getCacheDir(), "story_img_" + System.currentTimeMillis() + ".jpg");
                    FileOutputStream fos = new FileOutputStream(compressed);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
                    fos.close();
                    bitmap.recycle();
                    fileToUpload = compressed;

                } else {
                    // VIDEO: Sementara KIRIM ASLI (Kompres video butuh MediaCodec kompleks, nanti kita bikin)
                    InputStream is = context.getContentResolver().openInputStream(fileUri);
                    File original = new File(context.getCacheDir(), "story_vid_" + System.currentTimeMillis() + ".mp4");
                    FileOutputStream fos = new FileOutputStream(original);
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = is.read(buffer)) != -1) fos.write(buffer, 0, read);
                    fos.close();
                    is.close();
                    fileToUpload = original;
                }

                // 3. Kirim ke javin-cf
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

                java.io.FileInputStream fis = new java.io.FileInputStream(fileToUpload);
                byte[] buf = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buf)) != -1) os.write(buf, 0, bytesRead);
                fis.close();
                
                os.write(("\r\n--" + boundary + "--\r\n").getBytes());
                os.flush();
                os.close();

                // 4. Baca Response
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
