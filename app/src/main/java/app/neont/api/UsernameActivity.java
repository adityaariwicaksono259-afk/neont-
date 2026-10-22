package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import org.json.JSONObject;

public class UsernameActivity extends AppCompatActivity {
    private EditText etUsername;
    private Button btnSave;
    private ProgressBar spinner;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);
        mAuth = FirebaseAuth.getInstance();
        etUsername = findViewById(R.id.etUsername);
        btnSave = findViewById(R.id.btnSaveUsername);
        spinner = findViewById(R.id.spinner);

        ButtonHelper.setup(btnSave, spinner, etUsername);

        btnSave.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim().toLowerCase();

            // Validasi (minimal 3 karakter, hanya huruf/angka/underscore)
            if (username.isEmpty()) {
                Toast.makeText(this, "Username wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (username.length() < 3) {
                Toast.makeText(this, "Username minimal 3 karakter", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!username.matches("^[a-z0-9_]+$")) {
                Toast.makeText(this, "Hanya huruf, angka, dan _", Toast.LENGTH_SHORT).show();
                return;
            }

            ButtonHelper.showSpinner(btnSave, spinner);

            // 1. Simpan ke D1
            try {
                JSONObject fields = new JSONObject();
                fields.put("username", username);
                // Simpan juga display_name default = username
                fields.put("display_name", username);

                D1Helper.updateUser(mAuth.getUid(), fields, new D1Helper.UserCallback() {
                    @Override
                    public void onSuccess(JSONObject user) {
                        // 2. Firebase hanya untuk chat (users/uid/status), bukan profil.
                        Toast.makeText(UsernameActivity.this, "Username disimpan", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(UsernameActivity.this, HomeActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        ButtonHelper.hideSpinner(btnSave, spinner, "SIMPAN");
                        Toast.makeText(UsernameActivity.this, "Gagal: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                ButtonHelper.hideSpinner(btnSave, spinner, "SIMPAN");
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
