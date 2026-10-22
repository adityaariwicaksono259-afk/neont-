package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity {
    private EditText etGroupName;
    private RecyclerView rvContacts;
    private ContactAdapter adapter;
    private List<UserModel> contactList = new ArrayList<>();
    private Map<String, Boolean> selectedUids = new HashMap<>();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        mAuth = FirebaseAuth.getInstance();

        etGroupName = findViewById(R.id.etGroupName);
        rvContacts = findViewById(R.id.rvContacts);

        findViewById(R.id.btnBackCreate).setOnClickListener(v -> finish());

        adapter = new ContactAdapter(contactList, selectedUids);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.setAdapter(adapter);

        // Ambil data user dari Firebase (sementara, karena D1 butuh endpoint khusus)
        loadContacts();

        findViewById(R.id.btnCreateGroup).setOnClickListener(v -> createGroup());
    }

    private void loadContacts() {
        // Ambil semua user dari Firebase (kecuali diri sendiri)
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference().child("users")
            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    contactList.clear();
                    for (com.google.firebase.database.DataSnapshot data : snapshot.getChildren()) {
                        String uid = data.getKey();
                        if (uid != null && !uid.equals(mAuth.getUid())) {
                            String name = data.child("name").getValue(String.class);
                            contactList.add(new UserModel(uid, name, ""));
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
                @Override public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
            });
    }

    private void createGroup() {
        String title = etGroupName.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Nama grup wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        String groupId = FirebaseDatabase.getInstance().getReference().child("groups").push().getKey();
        if (groupId == null) return;

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("title", title);
        androidx.appcompat.widget.SwitchCompat sw = findViewById(R.id.swChannel);
        String typeExtra = getIntent().getStringExtra("type");
        groupData.put("type", typeExtra != null ? typeExtra : (sw != null && sw.isChecked() ? "channel" : "group"));
        groupData.put("createdBy", mAuth.getUid());
        groupData.put("timestamp", System.currentTimeMillis());

        Map<String, Boolean> members = new HashMap<>();
        members.put(mAuth.getUid(), true); // Admin masuk
        for (String uid : selectedUids.keySet()) {
            members.put(uid, true);
        }
        groupData.put("members", members);

        FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).setValue(groupData)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Grup dibuat", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, GroupInfoActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Adapter untuk daftar kontak
    private static class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
        private List<UserModel> list;
        private Map<String, Boolean> selected;

        ContactAdapter(List<UserModel> list, Map<String, Boolean> selected) {
            this.list = list;
            this.selected = selected;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int position) {
            UserModel user = list.get(position);
            h.tvName.setText(user.getName() != null ? user.getName() : "User");
            h.cbSelect.setChecked(selected.containsKey(user.getUid()));
            h.itemView.setOnClickListener(v -> {
                if (selected.containsKey(user.getUid())) selected.remove(user.getUid());
                else selected.put(user.getUid(), true);
                notifyItemChanged(position);
            });
        }

        @Override public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox cbSelect; TextView tvName;
            ViewHolder(View v) {
                super(v);
                cbSelect = v.findViewById(R.id.cbSelect);
                tvName = v.findViewById(R.id.tvContactName);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Mencegah memory leak
    }
}
