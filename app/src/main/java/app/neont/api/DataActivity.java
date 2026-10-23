package app.neont.api;

import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class DataActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        findViewById(R.id.btnBackData).setOnClickListener(v -> finish());

        // Tampilkan info penggunaan data
        TextView tvDataInfo = findViewById(R.id.tvDataInfo);
        if (tvDataInfo != null) {
            long cacheSize = getFolderSize(getCacheDir());
            long dataSize = getFolderSize(getFilesDir());
            String info = "Cache: " + formatSize(cacheSize) + "\n" +
                          "Data: " + formatSize(dataSize);
            tvDataInfo.setText(info);
        }

        findViewById(R.id.rowHapusCache).setOnClickListener(v -> {
            try {
                deleteDir(getCacheDir());
                Toast.makeText(this, "Cache berhasil dihapus", Toast.LENGTH_SHORT).show();
                recreate(); // Refresh halaman
            } catch (Exception e) {
                Toast.makeText(this, "Gagal hapus cache: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private long getFolderSize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    size += file.isDirectory() ? getFolderSize(file) : file.length();
                }
            }
        }
        return size;
    }

    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return (size / 1024) + " KB";
        return (size / (1024 * 1024)) + " MB";
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    deleteDir(new File(dir, child));
                }
            }
        }
        return dir != null && dir.delete();
    }
}
