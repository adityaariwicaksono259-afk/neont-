package app.neont.api;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class AddContactActivity extends AppCompatActivity {
    private EditText etUsername, etName;
    private FirebaseAuth mAuth;
    private DatabaseReference mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance().getReference();
        etUsername = findViewById(R.id.etUsername);
        etName = findViewById(R.id.etName);
        Button btnAdd = findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim().replace("@", "");
            String name = etName.getText().toString().trim();
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Isi username & nama", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cari user berdasarkan username
            mDb.child("users").orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            String uid = data.getKey();
                            Map<String, Object> contact = new HashMap<>();
                            contact.put("name", name);
                            contact.put("username", username);
                            mDb.child("contacts").child(mAuth.getUid()).child(uid).setValue(contact)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AddContactActivity.this, "Kontak ditambahkan!", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                        }
                    } else {
                        Toast.makeText(AddContactActivity.this, "Username tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onCancelled(DatabaseError error) {}
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Mencegah memory leak
    }
}
