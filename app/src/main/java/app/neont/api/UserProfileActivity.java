package app.neont.api;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvName, tvStatus, tvUsername;
    private DatabaseReference mDb;
    private String targetUid, myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        tvName = findViewById(R.id.tvUserName);
        tvStatus = findViewById(R.id.tvUserStatus);
        tvUsername = findViewById(R.id.tvUserUsername);

        mDb = FirebaseDatabase.getInstance().getReference();
        myUid = FirebaseAuth.getInstance().getUid();
        targetUid = getIntent().getStringExtra("uid");

        if (targetUid == null) { finish(); return; }

        // Load Data User
        mDb.child("users").child(targetUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);

                    tvName.setText(name != null ? name : "User");
                    tvUsername.setText(username != null ? "@" + username : "@user");
                    tvStatus.setText(status != null && status.equals("online") ? "online" : "tak terlihat sangat lama");
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

        // Tombol Obrolan
        findViewById(R.id.btnMessageUser).setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatRoomActivity.class);
            intent.putExtra("uid", targetUid);
            intent.putExtra("name", tvName.getText().toString());
            startActivity(intent);
        });

        // Tombol Menu (Blokir, Laporkan)
        findViewById(R.id.btnMenuProfile).setOnClickListener(v -> {
            String[] options = {"Blokir User", "Laporkan User"};
            new AlertDialog.Builder(this)
                .setTitle("Aksi")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) blokirUser();
                    else Toast.makeText(this, "Laporan terkirim", Toast.LENGTH_SHORT).show();
                })
                .show();
        });

        findViewById(R.id.btnBackProfile).setOnClickListener(v -> finish());
    }

    private void blokirUser() {
        mDb.child("blocks").child(myUid).child(targetUid).setValue(true)
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "User Diblokir", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(this, "Gagal blokir: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
