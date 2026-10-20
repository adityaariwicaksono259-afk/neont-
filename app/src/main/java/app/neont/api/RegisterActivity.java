package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etEmail = findViewById(R.id.etEmailReg);
        etPassword = findViewById(R.id.etPasswordReg);
        btnRegister = findViewById(R.id.btnDoRegister);

        btnRegister.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Isi semua kolom!", Toast.LENGTH_SHORT).show();
            return;
        }

        // VALIDASI PASSWORD KETAT
        if (pass.length() < 8) {
            Toast.makeText(this, "Password minimal 8 karakter!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Pattern.compile("[A-Z]").matcher(pass).find()) {
            Toast.makeText(this, "Password harus ada huruf besar!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Pattern.compile("[^a-zA-Z0-9]").matcher(pass).find()) {
            Toast.makeText(this, "Password harus ada simbol (contoh: @, #, !)", Toast.LENGTH_SHORT).show();
            return;
        }

        // PROSES DAFTAR KE FIREBASE
        mAuth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener(a -> {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    saveUserToDatabase(user);
                }
                Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void saveUserToDatabase(FirebaseUser user) {
        String uid = user.getUid();
        String email = user.getEmail();
        String name = email.split("@")[0];

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("name", name);
        userMap.put("status", "online");

        mDatabase.child("users").child(uid).setValue(userMap);
    }
}
