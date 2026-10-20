package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
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
import java.util.Random;

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

        fabNewChat.setOnClickListener(v -> {
            startActivity(new Intent(this, NewChatActivity.class));
        });

        // Pancing Kode Unik buat akun lama
        cekKodeUnik();
    }

    private void cekKodeUnik() {
        mDb.child("users").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && !snapshot.hasChild("code")) {
                    Random random = new Random();
                    String uniqueCode = "NEO-" + (1000 + random.nextInt(9000));
                    Map<String, Object> update = new HashMap<>();
                    update.put("code", uniqueCode);
                    mDb.child("users").child(myUid).updateChildren(update);
                    Toast.makeText(HomeActivity.this, "Akun lama lo dapet kode: " + uniqueCode, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}
