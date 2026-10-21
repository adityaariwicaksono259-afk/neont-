package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class PinActivity extends AppCompatActivity {
    private EditText etPin;
    private Button btnSave;
    private ProgressBar spinner;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);
        mAuth = FirebaseAuth.getInstance();
        etPin = findViewById(R.id.etPin);
        btnSave = findViewById(R.id.btnSavePin);
        spinner = findViewById(R.id.spinner);
        TextView tvKetentuan = findViewById(R.id.tvKetentuan);

        ButtonHelper.setup(btnSave, spinner, etPin);

        tvKetentuan.setOnClickListener(v -> startActivity(new Intent(this, TermsActivity.class)));

        btnSave.setOnClickListener(v -> {
            String pin = etPin.getText().toString().trim();
            if (pin.length() < 6) {
                Toast.makeText(this, "PIN minimal 6 digit!", Toast.LENGTH_SHORT).show();
                return;
            }

            ButtonHelper.showSpinner(btnSave, spinner);

            // HASH PIN SEBELUM DISIMPEN (KEAMANAN)
            String hashedPin = HashHelper.sha256(pin);

            Map<String, Object> update = new HashMap<>();
            update.put("pin", hashedPin);
            FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()).updateChildren(update)
                .addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(this, UsernameActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    ButtonHelper.hideSpinner(btnSave, spinner);
                    Toast.makeText(this, "Gagal simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });
    }
}
