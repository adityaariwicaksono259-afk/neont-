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

        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
        String osVersion = "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
        String abi = (Build.SUPPORTED_ABIS.length > 0) ? Build.SUPPORTED_ABIS[0] : "Unknown";

        tvDevice.setText(deviceName);
        tvOs.setText(osVersion + "\nABI: " + abi + "\nDevice ID: " + getMyDeviceId());
    }

    // Ganti nama dari getDeviceId() -> getMyDeviceId() agar tidak bentrok
    private String getMyDeviceId() {
        try {
            return android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
