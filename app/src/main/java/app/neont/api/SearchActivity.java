package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private RecyclerView rvResults;
    private ChatAdapter adapter;
    private List<UserModel> userList;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        rvResults = findViewById(R.id.rvResults);
        EditText etSearch = findViewById(R.id.etSearch);
        myUid = FirebaseAuth.getInstance().getUid();

        userList = new ArrayList<>();
        adapter = new ChatAdapter(this, userList, user -> {
            Intent intent = new Intent(this, ChatRoomActivity.class);
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
                String query = s.toString().trim();
                cariUserDiD1(query);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Tiru queryServerSearch() Telegram, tapi "server"-nya adalah D1 (jvin.pages.dev).
     * Endpoint: /api/neont/search?q=<username>
     */
    private void cariUserDiD1(String keyword) {
        if (keyword.isEmpty()) {
            userList.clear();
            adapter.notifyDataSetChanged();
            return;
        }
        if (keyword.length() < 2) return;

        D1Helper.searchUser(keyword, new D1Helper.SearchCallback() {
            @Override
            public void onSuccess(JSONArray users) {
                userList.clear();
                for (int i = 0; i < users.length(); i++) {
                    JSONObject u = users.optJSONObject(i);
                    if (u == null) continue;
                    String uid = u.optString("firebase_uid", "");
                    if (uid.isEmpty() || uid.equals(myUid)) continue;

                    String name = u.optString("display_name", "");
                    String username = u.optString("username", "");
                    userList.add(new UserModel(uid, name, username));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SearchActivity.this, "Cari gagal: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
