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
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private RecyclerView rvResults;
    private ChatAdapter adapter;
    private List<UserModel> userList;
    private DatabaseReference mDb;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        rvResults = findViewById(R.id.rvSearchResults);
        EditText etSearch = findViewById(R.id.etSearch);
        
        mDb = FirebaseDatabase.getInstance().getReference();
        myUid = FirebaseAuth.getInstance().getUid();
        
        userList = new ArrayList<>();
        adapter = new ChatAdapter(this, userList, user -> {
            Intent intent = new Intent(SearchActivity.this, ChatRoomActivity.class);
            intent.putExtra("uid", user.getUid());
            intent.putExtra("name", user.getName());
            startActivity(intent);
            finish();
        });
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                cariUser(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void cariUser(String keyword) {
        if (keyword.isEmpty()) {
            userList.clear();
            adapter.notifyDataSetChanged();
            return;
        }
        
        mDb.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String uid = data.getKey();
                    if (uid != null && !uid.equals(myUid)) {
                        String name = data.child("name").getValue(String.class);
                        String username = data.child("username").getValue(String.class);
                        
                        if (name != null && name.toLowerCase().contains(keyword.toLowerCase())) {
                            userList.add(new UserModel(uid, name, username));
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }
}
