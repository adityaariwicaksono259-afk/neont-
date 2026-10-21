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
import com.google.firebase.database.FirebaseDatabase;

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
                SharedPreferences prefs = getSharedPreferences("neont_device", Context.MODE_PRIVATE);
                String savedUid = prefs.getString("uid", null);

                if (savedUid != null && savedUid.equals(mAuth.getUid())) {
                    // Device lama, langsung ke Home
                    cekUsernameAtauKeHome();
                } else {
                    // Device baru, wajib PIN
                    startActivity(new Intent(this, PinActivity.class));
                    finish();
                }
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cekUsernameAtauKeHome() {
        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid())
            .child("username").get().addOnSuccessListener(snap -> {
                if (snap.exists() && snap.getValue(String.class) != null) {
                    // Simpan device ID & ke Home
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
