package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private ImageView ivTogglePass;
    private FirebaseAuth mAuth;
    private boolean isPassVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // KEAMANAN: Cegah screenshot di halaman login
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, 
                             android.view.WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivTogglePass = findViewById(R.id.ivTogglePass);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        ivTogglePass.setOnClickListener(v -> {
            if (isPassVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePass.setImageResource(R.drawable.ic_eye_open);
                isPassVisible = false;
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePass.setImageResource(R.drawable.ic_eye_closed);
                isPassVisible = true;
            }
            etPassword.setSelection(etPassword.length());
        });

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "Isi email & password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pass.length() < 8) {
                Toast.makeText(this, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(a -> {
                    // Cek D1 apakah username sudah ada
                    D1Helper.getUser(a.getUser().getUid(), new D1Helper.UserCallback() {
                        @Override public void onSuccess(org.json.JSONObject user) {
                            String username = user.optString("username", "");
                            if (username.isEmpty() || username.equals("null")) {
                                // Belum punya username, paksa buat
                                startActivity(new Intent(RegisterActivity.this, UsernameActivity.class));
                            } else {
                                // Sudah punya, langsung masuk
                                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                            }
                            finish();
                        }
                        @Override public void onError(String message) {
                            // Kalau error D1, anggap belum punya username
                            startActivity(new Intent(RegisterActivity.this, UsernameActivity.class));
                            finish();
                        }
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(this, AuthHelper.getErrorMessage(e), Toast.LENGTH_SHORT).show());
        });

        tvGoToLogin.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
    }
}
