package app.neont.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Kalau udah login, cek apakah udah punya username
        if (mAuth.getCurrentUser() != null) {
            cekUsernameAtauKeHome();
            return;
        }

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Isi email & password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener(a -> {
                // Cek apakah device ini udah pernah login (pake SharedPreferences)
                SharedPreferences prefs = getSharedPreferences("neont_device", Context.MODE_PRIVATE);
                String savedUid = prefs.getString("uid", null);

                if (savedUid != null && savedUid.equals(mAuth.getUid())) {
                    // Device lama, langsung ke Home
                    cekUsernameAtauKeHome();
                } else {
                    // Device baru, wajib OTP
                    sendOtp(email);
                }
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendOtp(String email) {
        new Thread(() -> {
            try {
                URL url = new URL("https://jvin.pages.dev/api/auth/email/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("email", email);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes());
                }

                runOnUiThread(() -> {
                    Intent intent = new Intent(this, OtpActivity.class);
                    intent.putExtra("email", email);
                    // Ambil username yang udah ada (kalau ada)
                    FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid())
                        .child("username").get().addOnSuccessListener(snap -> {
                            if (snap.exists()) intent.putExtra("target_username", snap.getValue(String.class));
                            startActivity(intent);
                            finish();
                        });
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Gagal kirim OTP: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void cekUsernameAtauKeHome() {
        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid())
            .child("username").get().addOnSuccessListener(snap -> {
                if (snap.exists() && snap.getValue(String.class) != null) {
                    // Udah punya username, simpan device ID, ke Home
                    SharedPreferences prefs = getSharedPreferences("neont_device", Context.MODE_PRIVATE);
                    prefs.edit().putString("uid", mAuth.getUid()).apply();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                } else {
                    // Belum punya username, wajib isi
                    startActivity(new Intent(this, UsernameActivity.class));
                    finish();
                }
            });
    }
}
