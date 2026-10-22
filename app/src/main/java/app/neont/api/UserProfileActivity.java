package app.neont.api;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONObject;

public class UserProfileActivity extends AppCompatActivity {
    private TextView tvName, tvStatus, tvUsername;
    private DatabaseReference mDb;
    private String targetUid, myUid;
    private boolean isSelf = false;

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

        if (targetUid == null) targetUid = myUid;
        if (targetUid == null) { finish(); return; }

        // LOGIKA PROFILEACTIVITY TELEGRAM: Cek apakah ini profil sendiri
        isSelf = targetUid.equals(myUid);

        // 1. DATA STATIS: Ambil dari D1
        D1Helper.getUser(targetUid, new D1Helper.UserCallback() {
            @Override
            public void onSuccess(JSONObject user) {
                String name = user.optString("display_name", "User");
                String username = user.optString("username", "");
                String bio = user.optString("bio", "");

                tvName.setText(name.isEmpty() ? "User" : name);
                tvUsername.setText(username.isEmpty() ? "@user" : "@" + username);
                
                if (!bio.isEmpty()) tvStatus.setText(bio);
            }

            @Override
            public void onError(String message) {
                tvName.setText("User");
                tvUsername.setText("@user");
            }
        });

        // 2. DATA REALTIME: Status online dari Firebase
        mDb.child("users").child(targetUid).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if ("online".equals(status)) {
                    if (tvStatus.getText().toString().isEmpty() || tvStatus.getText().toString().equals("terakhir dilihat baru-baru ini")) {
                        tvStatus.setText("online");
                    }
                } else if (tvStatus.getText().toString().isEmpty()) {
                    tvStatus.setText("terakhir dilihat baru-baru ini");
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

        // --- LOGIKA TOMBOL UTAMA (Obrolan / Edit Profil) ---
        LinearLayout btnMessage = findViewById(R.id.btnMessageUser);
        TextView tvBtnLabel = findTextView(btnMessage);
        ImageView ivBtnIcon = findImageView(btnMessage);

        if (isSelf) {
            // Profil Sendiri: ubah jadi "Edit Profil"
            if (tvBtnLabel != null) tvBtnLabel.setText("Edit Profil");
            if (ivBtnIcon != null) ivBtnIcon.setImageResource(android.R.drawable.ic_menu_edit);
            
            btnMessage.setOnClickListener(v -> {
                // TODO: Arahkan ke EditProfileActivity nanti
                Toast.makeText(this, "Fitur Edit Profil segera hadir", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Profil Orang Lain: "Obrolan" (Kirim Pesan)
            btnMessage.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatRoomActivity.class);
                intent.putExtra("uid", targetUid);
                intent.putExtra("name", tvName.getText().toString());
                startActivity(intent);
            });
        }

        // Menu Titik Tiga (Blokir / Laporkan)
        findViewById(R.id.btnMenuProfile).setOnClickListener(v -> {
            if (isSelf) {
                Toast.makeText(this, "Ini profil kamu", Toast.LENGTH_SHORT).show();
                return;
            }
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
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "User Diblokir", Toast.LENGTH_SHORT).show());
    }

    // Helper: cari TextView pertama di dalam LinearLayout (buat ubah label tombol)
    private TextView findTextView(LinearLayout parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChildAt(i) instanceof TextView) return (TextView) parent.getChildAt(i);
        }
        return null;
    }

    // Helper: cari ImageView pertama di dalam LinearLayout (buat ubah ikon tombol)
    private ImageView findImageView(LinearLayout parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChildAt(i) instanceof ImageView) return (ImageView) parent.getChildAt(i);
        }
        return null;
    }
}
