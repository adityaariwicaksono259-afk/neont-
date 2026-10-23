package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageView btnBack = findViewById(R.id.btnBackSettings);
        btnBack.setOnClickListener(v -> finish());

        // 1. Akun
        findViewById(R.id.rowAkun).setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        // 2. Privasi & Keamanan
        findViewById(R.id.rowPrivacy).setOnClickListener(v -> {
            startActivity(new Intent(this, PrivacyActivity.class));
        });

        // 3. Notifikasi (Dialog Pilihan)
        findViewById(R.id.rowNotif).setOnClickListener(v -> {
            String[] options = {"Terima Semua", "Silent (Getar Mati)", "Blokir Semua"};
            new android.app.AlertDialog.Builder(this)
                .setTitle("Pengaturan Notifikasi")
                .setItems(options, (dialog, which) -> {
                    String mode = which == 0 ? "all" : (which == 1 ? "silent" : "blocked");
                    getSharedPreferences("neont_settings", MODE_PRIVATE).edit().putString("notif_mode", mode).apply();
                    Toast.makeText(this, "Notifikasi: " + options[which], Toast.LENGTH_SHORT).show();
                }).show();
        });

        // 4. Data (Toast sementara, karena butuh Activity baru)
        findViewById(R.id.rowData).setOnClickListener(v -> {
            Toast.makeText(this, "Data & Penyimpanan: Segera hadir", Toast.LENGTH_SHORT).show();
        });

        // 5. Folder
        findViewById(R.id.rowFolder).setOnClickListener(v -> {
            Toast.makeText(this, "Folder Obrolan: Segera hadir", Toast.LENGTH_SHORT).show();
        });

        // 6. Perangkat (Buka DeviceActivity)
        findViewById(R.id.rowDevice).setOnClickListener(v -> {
            startActivity(new Intent(this, DeviceActivity.class));
        });
    }
}
