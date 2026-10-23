package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import org.json.JSONObject;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar spinner;
    private ImageView ivTogglePass;
    private FirebaseAuth mAuth;
    private boolean isPassVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // KEAMANAN: Cegah screenshot di halaman login
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, 
                             android.view.WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        spinner = findViewById(R.id.spinner);
        ivTogglePass = findViewById(R.id.ivTogglePass);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);

        ButtonHelper.setup(btnLogin, spinner, etEmail, etPassword);


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

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "Isi email & password", Toast.LENGTH_SHORT).show();
                return;
            }

            ButtonHelper.showSpinner(btnLogin, spinner);

            AuthHelper.login(this, email, pass, new AuthHelper.AuthCallback() {
                @Override
                public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                    // Cek D1 apakah username sudah ada
                    try {
                        D1Helper.getUser(user.getUid(), new D1Helper.UserCallback() {
                            @Override public void onSuccess(org.json.JSONObject u) {
                                String username = (u != null) ? u.optString("username", "") : "";
                                if (username.isEmpty() || username.equals("null")) {
                                    startActivity(new Intent(MainActivity.this, UsernameActivity.class));
                                } else {
                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                }
                                finish();
                            }
                            @Override public void onError(String message) {
                                startActivity(new Intent(MainActivity.this, UsernameActivity.class));
                                finish();
                            }
                        });
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Error D1: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, UsernameActivity.class));
                        finish();
                    }
                }

                @Override
                public void onError(String message) {
                    ButtonHelper.hideSpinner(btnLogin, spinner, "MASUK");
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
}
