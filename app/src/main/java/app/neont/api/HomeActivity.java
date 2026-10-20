package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
        FloatingActionButton fabNewChat = findViewById(R.id.fabNewChat);
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

        fabNewChat.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SearchActivity.class)));
        findViewById(R.id.navProfile).setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));

        chatList.add(new UserModel("GLOBAL", "NEONT REAL", "GLOBAL"));
        adapter.notifyDataSetChanged();

        cekUsernameLama();
    }

    // LOGIKA MIGRASI: Kasih username otomatis buat akun lama
    private void cekUsernameLama() {
        mDb.child("users").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && !snapshot.hasChild("username")) {
                    // Akun lama belum punya username
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
