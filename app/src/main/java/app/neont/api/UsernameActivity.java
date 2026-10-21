package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class UsernameActivity extends AppCompatActivity {

    private EditText etUsername;
    private String targetUsername, myUid;
    private DatabaseReference mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        etUsername = findViewById(R.id.etUsername);
        Button btnSave = findViewById(R.id.btnSaveUsername);
        mDb = FirebaseDatabase.getInstance().getReference();
        myUid = FirebaseAuth.getInstance().getUid();
        targetUsername = getIntent().getStringExtra("target_username");

        // Kalau device baru, username udah harus diisi (dari target)
        if (targetUsername != null) {
            etUsername.setText(targetUsername);
            etUsername.setEnabled(false); // Wajib sama persis
            btnSave.setText("VERIFIKASI & LANJUT");
        }

        btnSave.setOnClickListener(v -> {
            String input = etUsername.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Username wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (targetUsername != null && !input.equals(targetUsername)) {
                Toast.makeText(this, "Username harus sama persis!", Toast.LENGTH_SHORT).show();
                return;
            }

            saveUsername(input);
        });
    }

    private void saveUsername(String username) {
        // Cek ketersediaan username di database
        mDb.child("usernames").child(username).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists() && !snapshot.getValue(String.class).equals(myUid)) {
                Toast.makeText(this, "Username sudah dipakai!", Toast.LENGTH_SHORT).show();
            } else {
                // Simpan username
                mDb.child("usernames").child(username).setValue(myUid);
                Map<String, Object> update = new HashMap<>();
                update.put("username", username);
                mDb.child("users").child(myUid).updateChildren(update)
                    .addOnSuccessListener(aVoid -> {
                        startActivity(new Intent(this, HomeActivity.class));
                        finishAffinity();
                    });
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
