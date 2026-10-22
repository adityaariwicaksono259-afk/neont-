package app.neont.api;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import org.json.JSONObject;

public class EditProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        String uid = FirebaseAuth.getInstance().getUid();

        EditText etName = findViewById(R.id.etDisplayName);
        EditText etBio = findViewById(R.id.etBio);

        // Ambil data lama dari D1
        D1Helper.getUser(uid, new D1Helper.UserCallback() {
            @Override public void onSuccess(JSONObject user) {
                etName.setText(user.optString("display_name", ""));
                etBio.setText(user.optString("bio", ""));
            }
            @Override public void onError(String message) {}
        });

        findViewById(R.id.btnBackEdit).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnSaveEdit).setOnClickListener(v -> {
            try {
                JSONObject update = new JSONObject();
                update.put("display_name", etName.getText().toString());
                update.put("bio", etBio.getText().toString());
                
                D1Helper.updateUser(uid, update, new D1Helper.UserCallback() {
                    @Override public void onSuccess(JSONObject user) {
                        Toast.makeText(EditProfileActivity.this, "Profil diperbarui", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    @Override public void onError(String message) {
                        Toast.makeText(EditProfileActivity.this, "Gagal: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        });
    }
}
