package app.neont.api;

import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private LinearLayout loginBox;
    private FirebaseAuth mAuth;
    
    // Fitur suara ngetik
    private SoundPool soundPool;
    private int soundId;
    private boolean loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        loginBox = findViewById(R.id.loginBox);

        // Inisialisasi Suara
        soundPool = new SoundPool.Builder().setMaxStreams(1).build();
        // Load file suara (pastikan file typing_sound.mp3 ada di res/raw)
        try {
            soundId = soundPool.load(this, R.raw.typing_sound, 1);
            soundPool.setOnLoadCompleteListener((sp, id, status) -> loaded = true);
        } catch (Exception e) {
            loaded = false; // Abaikan kalau file nggak ada
        }

        // Pasang listener suara di EditText
        etEmail.addTextChangedListener(typingSoundWatcher);
        etPassword.addTextChangedListener(typingSoundWatcher);

        btnLogin.setOnClickListener(v -> login());
        
        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private final TextWatcher typingSoundWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (loaded && soundPool != null) {
                soundPool.play(soundId, 0.5f, 0.5f, 0, 0, 1f);
            }
        }
        @Override public void afterTextChanged(Editable s) {}
    };

    private void login() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Email & password wajib", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Animasi kotak login mengecil sebelum pindah halaman
        loginBox.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
        
        mAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener(a -> {
                Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);
                finish();
            })
            .addOnFailureListener(e -> {
                // Balikin kotak login kalau gagal
                loginBox.clearAnimation();
                Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
}
