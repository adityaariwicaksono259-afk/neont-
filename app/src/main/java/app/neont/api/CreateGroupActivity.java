package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity {
    private EditText etTitle, etDescription;
    private CheckBox cbChannel;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        mAuth = FirebaseAuth.getInstance();
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        cbChannel = findViewById(R.id.cbChannel);
        Button btnCreate = findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(this, "Nama wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            String groupId = FirebaseDatabase.getInstance().getReference().child("groups").push().getKey();
            if (groupId == null) return;
            Map<String, Object> groupData = new HashMap<>();
            groupData.put("title", title);
            groupData.put("description", desc);
            groupData.put("type", cbChannel.isChecked() ? "channel" : "group");
            groupData.put("adminId", mAuth.getUid());
            Map<String, Boolean> members = new HashMap<>();
            members.put(mAuth.getUid(), true);
            groupData.put("members", members);
            Map<String, Boolean> admins = new HashMap<>();
            admins.put(mAuth.getUid(), true);
            groupData.put("admins", admins);
            FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).setValue(groupData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Grup/Channel dibuat!", Toast.LENGTH_SHORT).show();
                    // Langsung ke GroupInfoActivity
                    Intent intent = new Intent(this, GroupInfoActivity.class);
                    intent.putExtra("groupId", groupId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}
