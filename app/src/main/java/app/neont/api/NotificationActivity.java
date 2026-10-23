package app.neont.api;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        findViewById(R.id.btnBackNotif).setOnClickListener(v -> finish());

        findViewById(R.id.btnAllNotif).setOnClickListener(v -> {
            setNotifMode("all");
            Toast.makeText(this, "Notifikasi: Terima Semua", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnSilentNotif).setOnClickListener(v -> {
            setNotifMode("silent");
            Toast.makeText(this, "Notifikasi: Silent", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnBlockNotif).setOnClickListener(v -> {
            setNotifMode("blocked");
            Toast.makeText(this, "Notifikasi: Blokir Semua", Toast.LENGTH_SHORT).show();
        });
    }

    private void setNotifMode(String mode) {
        getSharedPreferences("neont_settings", MODE_PRIVATE).edit().putString("notif_mode", mode).apply();
    }
}
