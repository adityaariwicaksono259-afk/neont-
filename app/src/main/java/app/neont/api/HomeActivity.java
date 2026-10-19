package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        Button btnLogout = findViewById(R.id.btnLogout);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvWelcome.setText("Login berhasil\n" + user.getEmail());
            
            // --- PANGGIL FUNGSI SIMPAN DATA DI SINI ---
            saveUserToDatabase(user);
        }

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    // Fungsi simpan data user dipindah ke sini
    private void saveUserToDatabase(FirebaseUser user) {
        String uid = user.getUid();
        String email = user.getEmail();
        String name = email.split("@")[0];

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("name", name);
        userMap.put("status", "online");

        mDatabase.child("users").child(uid).setValue(userMap)
            .addOnSuccessListener(aVoid -> Toast.makeText(HomeActivity.this, "Data tersimpan di DB!", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "DB Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
