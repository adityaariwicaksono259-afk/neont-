package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvBio, tvCode;
    private DatabaseReference mDb;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvName = findViewById(R.id.tvProfileName);
        tvBio = findViewById(R.id.tvProfileBio);
        tvCode = findViewById(R.id.tvProfileCode);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnLogout = findViewById(R.id.btnLogoutProfile);

        mDb = FirebaseDatabase.getInstance().getReference();
        myUid = FirebaseAuth.getInstance().getUid();

        // Load Data User
        mDb.child("users").child(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String code = snapshot.child("code").getValue(String.class);
                    tvName.setText(name != null ? name : "User");
                    tvCode.setText(code != null ? code : "Belum ada kode");
                    // Bio default sementara
                    tvBio.setText("Halo! Saya menggunakan Neont.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur Pengaturan segera hadir!", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finishAffinity();
        });
    }
}
