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
    private String currentTab = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvChats = findViewById(R.id.rvChats);
        EditText etSearch = findViewById(R.id.etSearchHome);
        tvBadge = findViewById(R.id.tvBadge);
        TextView tabAll = findViewById(R.id.tabAll);
        TextView tabUnread = findViewById(R.id.tabUnread);
        TextView tabNeon = findViewById(R.id.tabNeon);
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

        // Logika Tab Filter
        tabAll.setOnClickListener(v -> { currentTab = "all"; updateTabUI(tabAll, tabUnread, tabNeon); filterTab(); });
        tabUnread.setOnClickListener(v -> { currentTab = "unread"; updateTabUI(tabUnread, tabAll, tabNeon); filterTab(); });
        tabNeon.setOnClickListener(v -> { currentTab = "neon"; updateTabUI(tabNeon, tabAll, tabUnread); filterTab(); });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updateTabUI(TextView active, TextView... others) {
        active.setTextColor(getResources().getColor(R.color.tg_blue));
        active.setBackgroundResource(R.drawable.bg_tab_active);
        for (TextView other : others) {
            other.setTextColor(getResources().getColor(R.color.tg_text_gray));
            other.setBackgroundResource(0);
        }
    }

    private void filterTab() {
        userList.clear();
        if ("all".equals(currentTab)) {
            userList.addAll(allUsers);
        } else if ("unread".equals(currentTab)) {
            // Sementara: tampilin semua (nanti difilter unread)
            userList.addAll(allUsers);
        } else if ("neon".equals(currentTab)) {
            // Sementara: kosong (nanti tools dari javin-cf)
        }
        adapter.notifyDataSetChanged();
    }

    private void loadChats() {
        mDb.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allUsers.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String uid = data.getKey();
                    if (uid != null && !uid.equals(myUid)) {
                        String name = data.child("name").getValue(String.class);
                        allUsers.add(new UserModel(uid, name, ""));
                    }
                }
                filterTab();
                sortByLastMessage();
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    private void sortByLastMessage() {
        Map<String, Long> lastMessageTimes = new HashMap<>();
        for (UserModel user : allUsers) {
            String roomId = (myUid.compareTo(user.getUid()) < 0) ? myUid + "_" + user.getUid() : user.getUid() + "_" + myUid;
            mDb.child("chats").child(roomId).orderByChild("timestamp").limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        long time = 0;
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Long ts = data.child("timestamp").getValue(Long.class);
                            if (ts != null) time = ts;
                        }
                        lastMessageTimes.put(user.getUid(), time);
                        Collections.sort(userList, (u1, u2) -> {
                            Long t1 = lastMessageTimes.get(u1.getUid());
                            Long t2 = lastMessageTimes.get(u2.getUid());
                            if (t1 == null) t1 = 0L;
                            if (t2 == null) t2 = 0L;
                            return t2.compareTo(t1);
                        });
                        adapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(DatabaseError error) {}
                });
        }
    }

    private void loadUnreadCount() {
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
