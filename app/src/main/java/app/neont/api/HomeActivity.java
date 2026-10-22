package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView rvChats;
    private ChatAdapter adapter;
    private List<UserModel> userList, allUsers;
    private DatabaseReference mDb;
    private String myUid;
    private TextView tvBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvChats = findViewById(R.id.rvChats);
        EditText etSearch = findViewById(R.id.etSearchHome);
        tvBadge = findViewById(R.id.tvBadge);
        ImageView btnMenu = findViewById(R.id.btnMenuTop);
        mDb = FirebaseDatabase.getInstance().getReference();
        myUid = FirebaseAuth.getInstance().getUid();

        userList = new ArrayList<>();
        allUsers = new ArrayList<>();
        adapter = new ChatAdapter(this, userList, user -> {
            Intent intent = new Intent(this, ChatRoomActivity.class);
            intent.putExtra("uid", user.getUid());
            intent.putExtra("name", user.getName());
            startActivity(intent);
        });
        rvChats.setLayoutManager(new LinearLayoutManager(this));
        rvChats.setAdapter(adapter);

        // Titik tiga: buka CreateGroupActivity
        btnMenu.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateGroupActivity.class));
        });

        // Bottom Nav: Buka Profil Sendiri
        findViewById(R.id.btnNavProfile).setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("uid", myUid);
            startActivity(intent);
        });
        
        // Bottom Nav: Buka Kontak / Pengaturan (sementara toast dulu)
        findViewById(R.id.btnNavContacts).setOnClickListener(v -> 
            Toast.makeText(this, "Kontak segera hadir", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnNavSettings).setOnClickListener(v -> 
            Toast.makeText(this, "Pengaturan segera hadir", Toast.LENGTH_SHORT).show());

        loadChats();
        loadGroups();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadGroups() {
        mDb.child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    String groupId = data.getKey();
                    String title = data.child("title").getValue(String.class);
                    String type = data.child("type").getValue(String.class);
                    if (title != null && myUid != null) {
                        boolean isMember = data.child("members").hasChild(myUid);
                        if (isMember) {
                            UserModel group = new UserModel("group_" + groupId, title, type);
                            boolean exists = false;
                            for (UserModel u : allUsers) {
                                if (u.getUid().equals(group.getUid())) { exists = true; break; }
                            }
                            if (!exists) {
                                allUsers.add(group);
                                userList.add(group);
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    private void loadChats() {
        // LOGIKA DIALOGSACTIVITY TELEGRAM:
        // Hanya tampilkan room yang punya pesan terakhir.
        mDb.child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allUsers.clear();
                userList.clear();
                for (DataSnapshot room : snapshot.getChildren()) {
                    String roomId = room.getKey();
                    if (roomId == null || !roomId.contains("_")) continue;

                    // Ambil uid lawan chat dari roomId (format: uidA_uidB)
                    String[] parts = roomId.split("_");
                    if (parts.length != 2) continue;
                    String otherUid = parts[0].equals(myUid) ? parts[1] : parts[0];
                    if (otherUid.equals(myUid)) continue;

                    DataSnapshot messagesSnap = room.child("messages");
                    if (!messagesSnap.exists() || messagesSnap.getChildrenCount() == 0) continue;

                    // Ambil nama lawan chat dari users/
                    // (callback async, jadi kita simpan sementara dan update di listener)
                    final String fOtherUid = otherUid;
                    mDb.child("users").child(otherUid).child("name")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(DataSnapshot nameSnap) {
                                String name = nameSnap.getValue(String.class);
                                UserModel u = new UserModel(fOtherUid, name != null ? name : "User", "");
                                boolean exists = false;
                                for (UserModel ex : allUsers) {
                                    if (ex.getUid().equals(fOtherUid)) { exists = true; break; }
                                }
                                if (!exists) {
                                    allUsers.add(u);
                                    userList.add(u);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                            @Override public void onCancelled(DatabaseError error) {}
                        });
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    private void filter(String keyword) {
        userList.clear();
        if (keyword.isEmpty()) {
            userList.addAll(allUsers);
        } else {
            for (UserModel u : allUsers) {
                if (u.getName() != null && u.getName().toLowerCase().contains(keyword.toLowerCase())) {
                    userList.add(u);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
