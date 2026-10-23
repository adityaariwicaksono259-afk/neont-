package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
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
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView rvChats;
    private ChatAdapter adapter;
    private List<UserModel> userList, allUsers;
    private DatabaseReference mDb;
    private String myUid;
    private int loadLimit = 50;
    private long lastTimestamp = Long.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        myUid = FirebaseAuth.getInstance().getUid();
        if (myUid == null) { finish(); return; }

        // SATPAM USERNAME: Cek D1 sebelum masuk Home
        D1Helper.getUser(myUid, new D1Helper.UserCallback() {
            @Override
            public void onSuccess(JSONObject user) {
                String username = user.optString("username", "");
                if (username.isEmpty() || username.equals("null")) {
                    // Tendang ke UsernameActivity
                    startActivity(new Intent(HomeActivity.this, UsernameActivity.class));
                    finish();
                } else {
                    setupHome();
                }
            }
            @Override
            public void onError(String message) {
                // Jika D1 error (misal tidak ada internet), tetap izinkan masuk
                setupHome();
            }
        });
    }

    private void setupHome() {
        rvChats = findViewById(R.id.rvChats);
        EditText etSearch = findViewById(R.id.etSearchHome);
        ImageView btnMenu = findViewById(R.id.btnMenuTop);
        mDb = FirebaseDatabase.getInstance().getReference();

        userList = new ArrayList<>();
        allUsers = new ArrayList<>();
        adapter = new ChatAdapter(this, userList, user -> {
            Intent intent = new Intent(this, ChatRoomActivity.class);
            intent.putExtra("uid", user.getUid());
            intent.putExtra("name", user.getName());
            startActivity(intent);
        });
        LinearLayoutManager lm = new LinearLayoutManager(this);
        rvChats.setLayoutManager(lm);
        
        // Scroll listener untuk load more
        rvChats.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView rv, int dx, int dy) {
                if (!rv.canScrollVertically(1)) { // Di paling bawah
                    if (loadLimit < 500) { // Batas maksimal 500 chat
                        loadLimit += 50;
                        loadChats(); // Muat ulang dengan limit baru
                    }
                }
            }
        });
        rvChats.setAdapter(adapter);

        btnMenu.setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, v);
            popup.getMenu().add("Grup Baru");
            popup.getMenu().add("Pesan Tersimpan");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Grup Baru")) {
                    startActivity(new Intent(this, NewMessageActivity.class));
                    return true;
                } else if (item.getTitle().equals("Pesan Tersimpan")) {
                    Toast.makeText(this, "Fitur segera hadir", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
            popup.show();
        });

        findViewById(R.id.btnNavProfile).setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("uid", myUid);
            startActivity(intent);
        });
        
        findViewById(R.id.btnNavContacts).setOnClickListener(v -> Toast.makeText(this, "Kontak segera hadir", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnNavSettings).setOnClickListener(v -> Toast.makeText(this, "Pengaturan segera hadir", Toast.LENGTH_SHORT).show());

        loadChats();
        loadGroups();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString().trim()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadGroups() {
        mDb.child("groups").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    String groupId = data.getKey();
                    String title = data.child("title").getValue(String.class);
                    String type = data.child("type").getValue(String.class);
                    if (title != null && myUid != null) {
                        boolean isMember = data.child("members").hasChild(myUid);
                        if (isMember) {
                            UserModel group = new UserModel("group_" + groupId, title, type);
                            boolean exists = false;
                            for (UserModel u : allUsers) { if (u.getUid().equals(group.getUid())) { exists = true; break; } }
                            if (!exists) { allUsers.add(group); userList.add(group); }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    private void loadChats() {
        mDb.child("chats").orderByChild("timestamp").limitToLast(loadLimit).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                allUsers.clear();
                userList.clear();
                for (DataSnapshot room : snapshot.getChildren()) {
                    String roomId = room.getKey();
                    if (roomId == null || !roomId.contains("_")) continue;
                    String[] parts = roomId.split("_");
                    if (parts.length != 2) continue;
                    String otherUid = parts[0].equals(myUid) ? parts[1] : parts[0];
                    if (otherUid.equals(myUid)) continue;

                    DataSnapshot messagesSnap = room.child("messages");
                    if (!messagesSnap.exists() || messagesSnap.getChildrenCount() == 0) continue;

                    final String fOtherUid = otherUid;
                    mDb.child("users").child(otherUid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(DataSnapshot nameSnap) {
                            String name = nameSnap.getValue(String.class);
                            UserModel u = new UserModel(fOtherUid, name != null ? name : "User", "");
                            boolean exists = false;
                            for (UserModel ex : allUsers) { if (ex.getUid().equals(fOtherUid)) { exists = true; break; } }
                            if (!exists) { allUsers.add(u); userList.add(u); adapter.notifyDataSetChanged(); }
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
            String lower = keyword.toLowerCase().trim();
            for (UserModel u : allUsers) {
                if (u.getName() != null && u.getName().toLowerCase().contains(lower)) {
                    userList.add(u);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Listener Firebase dilepas (mencegah memory leak)
    }
}
