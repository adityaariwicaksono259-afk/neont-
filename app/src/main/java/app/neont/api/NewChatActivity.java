package app.neont.api;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.List;

public class NewChatActivity extends AppCompatActivity {

    private RecyclerView rvAllUsers;
    private ChatAdapter adapter;
    private List<UserModel> contactList;
    private DatabaseReference mDb;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_new_chat);
            
            rvAllUsers = findViewById(R.id.rvAllUsers);
            TextView menuNewContact = findViewById(R.id.menuNewContact);
            TextView menuNewGroup = findViewById(R.id.menuNewGroup);
            
            mDb = FirebaseDatabase.getInstance().getReference();
            myUid = FirebaseAuth.getInstance().getUid();

            rvAllUsers.setLayoutManager(new LinearLayoutManager(this));
            contactList = new ArrayList<>();
            
            adapter = new ChatAdapter(this, contactList, user -> {
                Intent intent = new Intent(NewChatActivity.this, ChatRoomActivity.class);
                intent.putExtra("uid", user.getUid());
                intent.putExtra("name", user.getName());
                startActivity(intent);
            });
            rvAllUsers.setAdapter(adapter);

            menuNewContact.setOnClickListener(v -> {
                startActivity(new Intent(this, AddContactActivity.class));
            });

            menuNewGroup.setOnClickListener(v -> {
                Toast.makeText(this, "Fitur Grup segera hadir!", Toast.LENGTH_SHORT).show();
            });

            loadSavedContacts();
            
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                .setTitle("Error di NewChatActivity")
                .setMessage(e.toString())
                .setPositiveButton("OK", (d, w) -> finish())
                .show();
        }
    }

    private void loadSavedContacts() {
        mDb.child("contacts").child(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                contactList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String uid = data.getKey();
                    String name = data.child("name").getValue(String.class);
                    String code = data.child("code").getValue(String.class);
                    if (uid != null && name != null) {
                        contactList.add(new UserModel(uid, name, code));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}
