package app.neont.api;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- CEK KEAMANAN DULU ---
        if (SecurityUtils.isRooted() || SecurityUtils.isEmulator()) {
            showBannedDialog();
            return; // Hentikan pembuatan UI
        }
        // -------------------------

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(v -> login());
        
        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void showBannedDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Akses Ditolak")
            .setMessage("Aplikasi ini tidak dapat berjalan di perangkat yang telah dimodifikasi (Root) atau Emulator.\n\nIni demi keamanan data Anda.")
            .setCancelable(false)
            .setPositiveButton("Keluar", (dialog, which) -> finishAffinity())
            .show();
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Email & password wajib", Toast.LENGTH_SHORT).show();
            return;
        }
        
        mAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener(a -> {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
