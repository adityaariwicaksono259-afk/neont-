package app.neont.api;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import org.json.JSONObject;

public class PrivacyActivity extends AppCompatActivity {
    private String myUid;
    private JSONObject userJson = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        myUid = FirebaseAuth.getInstance().getUid();

        D1Helper.getUser(myUid, new D1Helper.UserCallback() {
            @Override public void onSuccess(JSONObject user) { userJson = user; }
            @Override public void onError(String message) {}
        });

        findViewById(R.id.rowLastSeen).setOnClickListener(v -> showPrivacyDialog("last_seen", "Terakhir Terlihat"));
        findViewById(R.id.rowProfilePhoto).setOnClickListener(v -> showPrivacyDialog("profile_photo", "Foto Profil"));
        findViewById(R.id.rowCalls).setOnClickListener(v -> showPrivacyDialog("calls", "Panggilan"));
    }

    private void showPrivacyDialog(String field, String title) {
        String[] options = {"Semua Orang", "Kontak Saya", "Tidak Ada"};
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(options, (dialog, which) -> {
                String value = which == 0 ? "everybody" : (which == 1 ? "contacts" : "nobody");
                try {
                    JSONObject update = new JSONObject();
                    update.put("privacy_" + field, value);
                    D1Helper.updateUser(myUid, update, new D1Helper.UserCallback() {
                        @Override public void onSuccess(JSONObject user) {
                            Toast.makeText(PrivacyActivity.this, "Privasi diperbarui", Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onError(String message) {
                            Toast.makeText(PrivacyActivity.this, "Gagal: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) { e.printStackTrace(); }
            })
            .show();
    }
}
