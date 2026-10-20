package app.neont.api;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDb;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance().getReference();
        myUid = mAuth.getUid();

        Button btnPassword = findViewById(R.id.btnUbahPassword);
        Button btnPrivasi = findViewById(R.id.btnPrivasi);
        Button btnLogoutAll = findViewById(R.id.btnLogoutAll);

        // Logika Ganti Password
        btnPassword.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ubah Password");
            final EditText input = new EditText(this);
            input.setHint("Password Baru");
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);
            builder.setPositiveButton("SIMPAN", (dialog, which) -> {
                String newPass = input.getText().toString().trim();
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && newPass.length() >= 8) {
                    user.updatePassword(newPass)
                        .addOnSuccessListener(a -> Toast.makeText(this, "Password berhasil diubah!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show());
                } else {
                    Toast.makeText(this, "Password minimal 8 karakter!", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("BATAL", null);
            builder.show();
        });

        // Logika Privasi Last Seen
        btnPrivasi.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Privasi Last Seen");
            String[] options = {"Semua Orang", "Hanya Kontak", "Tidak Ada"};
            builder.setItems(options, (dialog, which) -> {
                Map<String, Object> update = new HashMap<>();
                update.put("last_seen_privacy", options[which]);
                mDb.child("users").child(myUid).updateChildren(update);
                Toast.makeText(this, "Privasi diatur ke: " + options[which], Toast.LENGTH_SHORT).show();
            });
            builder.show();
        });

        // Logika Logout dari Semua Device (Multi-akun)
        btnLogoutAll.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Keluar dari Semua Device")
                .setMessage("Anda akan keluar dari semua perangkat yang login.")
                .setPositiveButton("KELUAR", (d, w) -> {
                    mAuth.signOut();
                    finishAffinity();
                })
                .setNegativeButton("BATAL", null)
                .show();
        });
    }
}
