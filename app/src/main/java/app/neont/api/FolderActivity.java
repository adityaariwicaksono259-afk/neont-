package app.neont.api;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class FolderActivity extends AppCompatActivity {
    private TextView tvFolderList;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        findViewById(R.id.btnBackFolder).setOnClickListener(v -> finish());

        prefs = getSharedPreferences("neont_folders", MODE_PRIVATE);
        tvFolderList = findViewById(R.id.tvFolderList);
        loadFolders();

        // Klik untuk buat folder baru
        tvFolderList.setOnClickListener(v -> showAddFolderDialog());
    }

    private void loadFolders() {
        String folders = prefs.getString("folder_list", "Belum ada folder");
        if (folders.isEmpty() || folders.equals("Belum ada folder")) {
            tvFolderList.setText("Belum ada folder. Ketuk untuk membuat.");
        } else {
            tvFolderList.setText(folders);
        }
    }

    private void showAddFolderDialog() {
        EditText input = new EditText(this);
        input.setHint("Nama Folder");
        new AlertDialog.Builder(this)
            .setTitle("Buat Folder Baru")
            .setView(input)
            .setPositiveButton("Buat", (dialog, which) -> {
                String folderName = input.getText().toString().trim();
                if (!folderName.isEmpty()) {
                    String existing = prefs.getString("folder_list", "");
                    if (existing.isEmpty() || existing.equals("Belum ada folder")) {
                        existing = folderName;
                    } else {
                        existing = existing + ", " + folderName;
                    }
                    prefs.edit().putString("folder_list", existing).apply();
                    loadFolders();
                    Toast.makeText(this, "Folder dibuat: " + folderName, Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }
}
