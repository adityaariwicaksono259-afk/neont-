package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class AddContactActivity extends AppCompatActivity {

    private EditText etCode, etName;
    private Button btnSave;
    private DatabaseReference mDb;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        mDb = FirebaseDatabase.getInstance().getReference();
        myUid = FirebaseAuth.getInstance().getUid();

        etCode = findViewById(R.id.etContactCode);
        etName = findViewById(R.id.etContactName);
        btnSave = findViewById(R.id.btnSaveContact);

        btnSave.setOnClickListener(v -> simpanKontak());
    }

    private void simpanKontak() {
        String code = etCode.getText().toString().trim().toUpperCase();
        String name = etName.getText().toString().trim();

        if (TextUtils.isEmpty(code) || TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Kode dan Nama wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cari user berdasarkan kode di database
        mDb.child("users").orderByChild("code").equalTo(code).get()
            .addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        String targetUid = userSnap.getKey();
                        if (targetUid != null && targetUid.equals(myUid)) {
                            Toast.makeText(this, "Ini kode lo sendiri, Tuan!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Simpan kontak ke database milik kita
                        Map<String, Object> contactMap = new HashMap<>();
                        contactMap.put("uid", targetUid);
                        contactMap.put("name", name);
                        contactMap.put("code", code);
                        
                        mDb.child("contacts").child(myUid).child(targetUid).setValue(contactMap)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Kontak tersimpan!", Toast.LENGTH_SHORT).show();
                                finish(); // Balik ke Home
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Gagal simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Toast.makeText(this, "Kode tidak ditemukan. Pastikan kode benar.", Toast.LENGTH_LONG).show();
                }
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Error database: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
