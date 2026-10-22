package app.neont.api;

import android.content.Intent;
import android.app.AlertDialog;
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

        findViewById(R.id.rowAkun).setOnClickListener(v -> 
            Toast.makeText(this, "Fitur Akun segera hadir", Toast.LENGTH_SHORT).show());
        
        findViewById(R.id.rowPrivacy).setOnClickListener(v -> 
            startActivity(new Intent(this, PrivacyActivity.class)));

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

        findViewById(R.id.rowData).setOnClickListener(v -> 
            Toast.makeText(this, "Data & Penyimpanan: Segera hadir", Toast.LENGTH_SHORT).show());

        findViewById(R.id.rowFolder).setOnClickListener(v -> 
            Toast.makeText(this, "Folder Obrolan: Segera hadir", Toast.LENGTH_SHORT).show());

        findViewById(R.id.rowDevice).setOnClickListener(v -> 
            startActivity(new Intent(this, DeviceActivity.class)));
    }
}
