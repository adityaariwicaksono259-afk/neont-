package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageView btnBack = findViewById(R.id.btnBackSettings);
        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.rowAkun).setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        findViewById(R.id.rowPrivacy).setOnClickListener(v -> {
            startActivity(new Intent(this, PrivacyActivity.class));
        });

        findViewById(R.id.rowNotif).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationActivity.class));
        });

        findViewById(R.id.rowData).setOnClickListener(v -> {
            startActivity(new Intent(this, DataActivity.class));
        });

        findViewById(R.id.rowFolder).setOnClickListener(v -> {
            startActivity(new Intent(this, FolderActivity.class));
        });

        findViewById(R.id.rowDevice).setOnClickListener(v -> {
            startActivity(new Intent(this, DeviceActivity.class));
        });
    }
}
