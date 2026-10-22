package app.neont.api;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DeviceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        findViewById(R.id.btnBackDevice).setOnClickListener(v -> finish());
        
        TextView tvDevice = findViewById(R.id.tvDeviceName);
        TextView tvOs = findViewById(R.id.tvDeviceOs);

        tvDevice.setText("Perangkat ini: " + Build.MANUFACTURER + " " + Build.MODEL);
        tvOs.setText("Android " + Build.VERSION.RELEASE);
    }
}
