package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OtpActivity extends AppCompatActivity {
    private EditText etOtp;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        etOtp = findViewById(R.id.etOtp);
        Button btnVerify = findViewById(R.id.btnVerify);
        TextView tvInfo = findViewById(R.id.tvInfo);

        email = getIntent().getStringExtra("email");
        tvInfo.setText("Kode dikirim ke " + email);

        // Kirim OTP saat halaman dibuka
        kirimOtp();

        btnVerify.setOnClickListener(v -> {
            String code = etOtp.getText().toString().trim();
            if (code.length() < 6) {
                Toast.makeText(this, "Masukkan 6 digit OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            verifikasiOtp(code);
        });
    }

    private void kirimOtp() {
        new Thread(() -> {
            try {
                URL url = new URL("https://jvin.pages.dev/api/auth/email/send-otp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("email", email);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes());
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    runOnUiThread(() -> Toast.makeText(OtpActivity.this, "OTP terkirim, cek email!", Toast.LENGTH_LONG).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(OtpActivity.this, "Gagal kirim OTP", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(OtpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void verifikasiOtp(String code) {
        new Thread(() -> {
            try {
                URL url = new URL("https://jvin.pages.dev/api/auth/email/verify-otp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("code", code);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes());
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(OtpActivity.this, "OTP Valid!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(OtpActivity.this, PinActivity.class));
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(OtpActivity.this, "OTP Salah/Kadaluarsa", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(OtpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
