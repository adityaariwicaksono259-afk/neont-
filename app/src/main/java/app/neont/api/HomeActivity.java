package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        loadChats();
        loadUnreadCount();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadChats() {
        mDb.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allUsers.clear();
                userList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String uid = data.getKey();
                    if (uid != null && !uid.equals(myUid)) {
                        String name = data.child("name").getValue(String.class);
                        allUsers.add(new UserModel(uid, name, ""));
                        userList.add(new UserModel(uid, name, ""));
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    // LOGIKA TELEGRAM: Unread Count (Badge)
    private void loadUnreadCount() {
        // Baca semua chat, hitung yang status != read
        mDb.child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int unread = 0;
                for (DataSnapshot room : snapshot.getChildren()) {
                    for (DataSnapshot msg : room.getChildren()) {
                        String receiver = msg.child("receiverId").getValue(String.class);
                        String status = msg.child("status").getValue(String.class);
                        if (myUid != null && myUid.equals(receiver) && !"read".equals(status)) {
                            unread++;
                        }
                    }
                }
                if (unread > 0) {
                    tvBadge.setVisibility(View.VISIBLE);
                    tvBadge.setText(unread > 99 ? "99+" : String.valueOf(unread));
                } else {
                    tvBadge.setVisibility(View.GONE);
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
