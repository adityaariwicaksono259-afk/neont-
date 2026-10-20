package app.neont.api;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

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
                    String bio = snapshot.child("bio").getValue(String.class);
                    String code = snapshot.child("code").getValue(String.class);
                    
                    tvName.setText(name != null ? name : "User");
                    tvCode.setText(code != null ? code : "Belum ada kode");
                    tvBio.setText(bio != null ? bio : "Halo! Saya menggunakan Neont.");
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });

        // Logika Ganti Nama
        tvName.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ganti Nama");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(tvName.getText().toString());
            builder.setView(input);
            builder.setPositiveButton("SIMPAN", (dialog, which) -> {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    Map<String, Object> update = new HashMap<>();
                    update.put("name", newName);
                    mDb.child("users").child(myUid).updateChildren(update);
                }
            });
            builder.setNegativeButton("BATAL", null);
            builder.show();
        });

        // Logika Ganti Bio
        tvBio.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ganti Bio");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(tvBio.getText().toString());
            builder.setView(input);
            builder.setPositiveButton("SIMPAN", (dialog, which) -> {
                String newBio = input.getText().toString().trim();
                Map<String, Object> update = new HashMap<>();
                update.put("bio", newBio);
                mDb.child("users").child(myUid).updateChildren(update);
            });
            builder.setNegativeButton("BATAL", null);
            builder.show();
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
