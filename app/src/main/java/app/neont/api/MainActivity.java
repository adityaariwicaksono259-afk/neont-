package app.neont.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        if (mAuth.getCurrentUser() != null) {
            cekUserLama();
            return;
        }

        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> register());
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Isi email & password", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener(a -> cekUserLama())
            .addOnFailureListener(e -> Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void register() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Isi email & password", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener(a -> cekUserLama())
            .addOnFailureListener(e -> Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cekUserLama() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        FirebaseDatabase.getInstance().getReference().child("users").child(uid).get()
            .addOnSuccessListener(snapshot -> {
                boolean hasUsername = snapshot.hasChild("username") && snapshot.child("username").getValue(String.class) != null;
                boolean hasPin = snapshot.hasChild("pin") && snapshot.child("pin").getValue(String.class) != null;

                SharedPreferences prefs = getSharedPreferences("neont_device", Context.MODE_PRIVATE);
                String savedUid = prefs.getString("uid", null);
                boolean isDeviceLama = (savedUid != null && savedUid.equals(uid));

                if (hasUsername && hasPin && isDeviceLama) {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                } else if (!hasPin) {
                    startActivity(new Intent(this, PinActivity.class));
                    finish();
                } else if (!hasUsername) {
                    startActivity(new Intent(this, UsernameActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(this, PinActivity.class));
                    finish();
                }
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
