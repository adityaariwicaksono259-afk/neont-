package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import org.json.JSONObject;

public class UserProfileActivity extends AppCompatActivity {
    private TextView tvName, tvUsername;
    private ImageView ivPhoto;
    private String targetUid, myUid;
    private boolean isSelf = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        tvName = findViewById(R.id.tvUserName);
        tvUsername = findViewById(R.id.tvUserUsername);
        ivPhoto = findViewById(R.id.ivUserPhoto);
        myUid = FirebaseAuth.getInstance().getUid();
        targetUid = getIntent().getStringExtra("uid");

        if (targetUid == null) targetUid = myUid;
        if (targetUid == null) { finish(); return; }

        isSelf = targetUid.equals(myUid);

        // Ambil data REAL dari D1
        D1Helper.getUser(targetUid, new D1Helper.UserCallback() {
            @Override
            public void onSuccess(JSONObject user) {
                String name = user.optString("display_name", "User");
                String username = user.optString("username", "");

                tvName.setText(name.isEmpty() ? "User" : name);
                tvUsername.setText(username.isEmpty() ? "@user" : "@" + username);
            }
            @Override public void onError(String message) {}
        });

        // Setup Tombol Aksi (Self vs Other)
        LinearLayout layoutSelf = findViewById(R.id.layoutSelfActions);
        LinearLayout layoutSettings = findViewById(R.id.layoutSettingsList);
        Button btnMessage = findViewById(R.id.btnMessageUser);

        if (isSelf) {
            layoutSelf.setVisibility(View.VISIBLE);
            layoutSettings.setVisibility(View.VISIBLE);
            btnMessage.setVisibility(View.GONE);

            findViewById(R.id.btnSetPhoto).setOnClickListener(v -> Toast.makeText(this, "Fitur Pasang Foto", Toast.LENGTH_SHORT).show());
            findViewById(R.id.btnEditInfo).setOnClickListener(v -> Toast.makeText(this, "Fitur Edit Info", Toast.LENGTH_SHORT).show());
            findViewById(R.id.btnSettings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
            findViewById(R.id.rowEditProfile).setOnClickListener(v -> Toast.makeText(this, "Fitur Edit Profil", Toast.LENGTH_SHORT).show());
        } else {
            layoutSelf.setVisibility(View.GONE);
            layoutSettings.setVisibility(View.GONE);
            btnMessage.setVisibility(View.VISIBLE);
            btnMessage.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatRoomActivity.class);
                intent.putExtra("uid", targetUid);
                intent.putExtra("name", tvName.getText().toString());
                startActivity(intent);
            });
        }
    }
}
