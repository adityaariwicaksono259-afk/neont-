package app.neont.api;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CrashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
        
        // Matikan FLAG_SECURE agar layar error bisa di-screenshot
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);

        String error = getIntent().getStringExtra("error");
        TextView tv = findViewById(R.id.tvError);
        tv.setText(error != null ? error : "Unknown Error");

        Button btnClose = findViewById(R.id.btnCloseCrash);
        btnClose.setOnClickListener(v -> {
            finishAffinity();
            android.os.Process.killProcess(android.os.Process.myPid());
        });
    }
}
