package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupInfoActivity extends AppCompatActivity {
    private String groupId;
    private DatabaseReference mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        mDb = FirebaseDatabase.getInstance().getReference();
        groupId = getIntent().getStringExtra("groupId");

        TextView tvName = findViewById(R.id.tvGroupName);
        TextView tvDesc = findViewById(R.id.tvGroupDesc);
        TextView tvMembers = findViewById(R.id.tvGroupMembers);
        Button btnChat = findViewById(R.id.btnChatGroup);
        Button btnAdd = findViewById(R.id.btnAddMember);

        mDb.child("groups").child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvName.setText(snapshot.child("title").getValue(String.class));
                    tvDesc.setText(snapshot.child("description").getValue(String.class));
                    long memberCount = snapshot.child("members").getChildrenCount();
                    tvMembers.setText(memberCount + " Anggota");
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

        // Tombol Share Link
        findViewById(R.id.btnShowQr).setOnClickListener(v -> {
            String link = "https://neont.app/g/" + groupId;
            // Buka Activity QR (kita buat nanti)
            android.content.Intent intent = new android.content.Intent(this, QrActivity.class);
            intent.putExtra("link", link);
            startActivity(intent);
        });

        findViewById(R.id.btnShareLink).setOnClickListener(v -> {
            String link = "https://neont.app/g/" + groupId;
            android.content.Intent share = new android.content.Intent(android.content.Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(android.content.Intent.EXTRA_TEXT, "Gabung ke grup NEONT: " + link);
            startActivity(android.content.Intent.createChooser(share, "Bagikan Link Grup"));
        });

        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatRoomActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("name", tvName.getText().toString());
            startActivity(intent);
        });

        btnAdd.setOnClickListener(v -> {
            // Nanti kita bikin AddMemberActivity
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Mencegah memory leak
    }
}
