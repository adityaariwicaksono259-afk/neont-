package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

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
            String username = etUsername.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Username wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            ButtonHelper.showSpinner(btnSave, spinner);

            Map<String, Object> update = new HashMap<>();
            update.put("username", username);
            FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()).updateChildren(update)
                .addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    ButtonHelper.hideSpinner(btnSave, spinner, "SIMPAN");
                    Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });
    }
}
