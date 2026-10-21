package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnRegister;
    private ProgressBar spinner;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        spinner = findViewById(R.id.spinner);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        ButtonHelper.setup(btnRegister, spinner, etEmail, etPassword);

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) return;
            if (pass.length() < 8 || !Pattern.compile("[A-Z]").matcher(pass).find() || !Pattern.compile("[a-z]").matcher(pass).find() || !Pattern.compile("[0-9]").matcher(pass).find() || !Pattern.compile("[^a-zA-Z0-9]").matcher(pass).find()) {
                Toast.makeText(this, "Password lemah! (Min 8, huruf besar, kecil, angka, simbol)", Toast.LENGTH_LONG).show();
                return;
            }

            ButtonHelper.showSpinner(btnRegister, spinner);

            mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(a -> {
                    startActivity(new Intent(this, OtpActivity.class).putExtra("email", email));
                    finish();
                })
                .addOnFailureListener(e -> {
                    ButtonHelper.hideSpinner(btnRegister, spinner);
                    Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });

        tvGoToLogin.setOnClickListener(v -> { startActivity(new Intent(this, MainActivity.class)); finish(); });
    }
}
