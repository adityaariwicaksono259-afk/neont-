package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvChats;
    private ChatAdapter adapter;
    private List<UserModel> chatList; // Nanti diisi dari riwayat chat
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvChats = findViewById(R.id.rvChats);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        FloatingActionButton fabNewChat = findViewById(R.id.fabNewChat);
        myUid = FirebaseAuth.getInstance().getUid();
        
        // Setup RecyclerView (Kosong dulu, karena belum ada chat)
        rvChats.setLayoutManager(new LinearLayoutManager(this));
        chatList = new ArrayList<>();
        adapter = new ChatAdapter(this, chatList, user -> {
            Intent intent = new Intent(HomeActivity.this, ChatRoomActivity.class);
            intent.putExtra("uid", user.getUid());
            intent.putExtra("name", user.getName());
            startActivity(intent);
        });
        rvChats.setAdapter(adapter);

        // Tombol (+) buat buka halaman Pilih Kontak
        fabNewChat.setOnClickListener(v -> {
            startActivity(new Intent(this, NewChatActivity.class));
        });

        // Handle klik bottom nav
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_chat) {
                return true;
            } else {
                Toast.makeText(this, "Fitur ini segera hadir!", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }
}
