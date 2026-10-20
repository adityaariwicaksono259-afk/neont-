package app.neont.api;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvChats;
    private ChatAdapter adapter;
    private List<UserModel> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rvChats = findViewById(R.id.rvChats);
        FloatingActionButton fabNewChat = findViewById(R.id.fabNewChat);
        
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
            startActivity(new Intent(HomeActivity.this, SearchActivity.class));
        });

        findViewById(R.id.navProfile).setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));

        // Inject Chat Global
        chatList.add(new UserModel("GLOBAL", "NEONT REAL", "GLOBAL"));
        adapter.notifyDataSetChanged();
    }
}
