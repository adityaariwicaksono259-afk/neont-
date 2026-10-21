package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmailReg);
        etPassword = findViewById(R.id.etPasswordReg);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.btn_click));
            register();
        });

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void register() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Isi email & password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.length() < 8) {
            Toast.makeText(this, "Password minimal 8 karakter!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Pattern.compile("[A-Z]").matcher(pass).find()) {
            Toast.makeText(this, "Password harus ada huruf besar!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Pattern.compile("[a-z]").matcher(pass).find()) {
            Toast.makeText(this, "Password harus ada huruf kecil!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Pattern.compile("[0-9]").matcher(pass).find()) {
            Toast.makeText(this, "Password harus ada angka!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Pattern.compile("[^a-zA-Z0-9]").matcher(pass).find()) {
            Toast.makeText(this, "Password harus ada simbol (contoh: @, #, !)", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener(a -> {
                Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, PinActivity.class));
                finish();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
