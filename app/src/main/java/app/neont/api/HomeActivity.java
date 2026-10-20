package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvChats;
    private ChatAdapter adapter;
    private List<UserModel> chatList;
    private DatabaseReference mDb;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvChats = findViewById(R.id.rvChats);
        EditText etHomeSearch = findViewById(R.id.etHomeSearch);
        mDb = FirebaseDatabase.getInstance().getReference();
        myUid = FirebaseAuth.getInstance().getUid();
        
        rvChats.setLayoutManager(new LinearLayoutManager(this));
        chatList = new ArrayList<>();
        adapter = new ChatAdapter(this, chatList, user -> {
            Intent intent = new Intent(HomeActivity.this, ChatRoomActivity.class);
            intent.putExtra("uid", user.getUid());
            intent.putExtra("name", user.getName());
            startActivity(intent);
        });
        rvChats.setAdapter(adapter);

        findViewById(R.id.navProfile).setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        findViewById(R.id.navPembaruan).setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, PembaruanActivity.class)));

        // Listener Search di Home
        etHomeSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    muatChatDefault();
                } else {
                    cariUser(keyword);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        muatChatDefault();
        cekUsernameLama();
    }

    private void muatChatDefault() {
        chatList.clear();
        // Chat Global Selalu Ada
        chatList.add(new UserModel("GLOBAL", "NEONT REAL", "GLOBAL"));
        adapter.notifyDataSetChanged();
    }

    private void cariUser(String keyword) {
        mDb.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String uid = data.getKey();
                    if (uid != null && !uid.equals(myUid)) {
                        String name = data.child("name").getValue(String.class);
                        String username = data.child("username").getValue(String.class);
                        
                        if (name != null && name.toLowerCase().contains(keyword.toLowerCase())) {
                            chatList.add(new UserModel(uid, name, username));
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    private void cekUsernameLama() {
        mDb.child("users").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && !snapshot.hasChild("username")) {
                    String email = snapshot.child("email").getValue(String.class);
                    if (email != null) {
                        String autoUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "") + "_" + (1000 + (int)(Math.random()*9000));
                        Map<String, Object> update = new HashMap<>();
                        update.put("username", autoUsername);
                        mDb.child("users").child(myUid).updateChildren(update);
                        mDb.child("usernames").child(autoUsername).setValue(myUid);
                    }
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }
}
