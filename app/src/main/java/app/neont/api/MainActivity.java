package app.neont.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.database.FirebaseDatabase;

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
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) return;

            ButtonHelper.showSpinner(btnLogin, spinner);

            mAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(a -> {
                    String uid = mAuth.getUid();
                    SharedPreferences prefs = getSharedPreferences("neont_device", Context.MODE_PRIVATE);
                    String savedUid = prefs.getString("uid", null);
                    boolean isDeviceLama = (savedUid != null && savedUid.equals(uid));

                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).get()
                        .addOnSuccessListener(snapshot -> {
                            boolean hasPin = snapshot.hasChild("pin") && snapshot.child("pin").getValue(String.class) != null;
                            boolean hasUsername = snapshot.hasChild("username") && snapshot.child("username").getValue(String.class) != null;

                            // SIMPEN DEVICE ID (KUNCI!)
                            prefs.edit().putString("uid", uid).apply();

                            if (isDeviceLama && hasPin && hasUsername) {
                                startActivity(new Intent(this, HomeActivity.class));
                                finish();
                            } else if (!hasPin) {
                                startActivity(new Intent(this, PinActivity.class));
                                finish();
                            } else if (!hasUsername) {
                                startActivity(new Intent(this, UsernameActivity.class));
                                finish();
                            } else {
                                startActivity(new Intent(this, OtpActivity.class).putExtra("email", email));
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> {
                            ButtonHelper.hideSpinner(btnLogin, spinner);
                            Toast.makeText(this, "Error DB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                })
                .addOnFailureListener(e -> {
                    ButtonHelper.hideSpinner(btnLogin, spinner);
                    Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });

        tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
}
