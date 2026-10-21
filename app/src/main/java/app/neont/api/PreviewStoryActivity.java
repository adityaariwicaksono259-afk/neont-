package app.neont.api;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class PreviewStoryActivity extends AppCompatActivity {

    private ImageView ivStoryPreview, btnPickImage, btnPostStory, btnBack;
    private EditText etStoryCaption;
    private Uri selectedMediaUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_story);

        ivStoryPreview = findViewById(R.id.ivStoryPreview);
        etStoryCaption = findViewById(R.id.etStoryCaption);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnPostStory = findViewById(R.id.btnPostStory);
        btnBack = findViewById(R.id.btnBackPreview);

        ActivityResultLauncher<Intent> pickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedMediaUri = result.getData().getData();
                    ivStoryPreview.setImageURI(selectedMediaUri);
                }
            }
        );

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
            pickerLauncher.launch(intent);
        });

        btnPostStory.setOnClickListener(v -> {
            if (selectedMediaUri == null) {
                Toast.makeText(this, "Pilih foto/video dulu!", Toast.LENGTH_SHORT).show();
                return;
            }
            pilihPrivasiDanUpload();
        });
        
        btnBack.setOnClickListener(v -> finish());
    }

    private void pilihPrivasiDanUpload() {
        String[] options = {"Semua Orang (Publik)", "Kontak Saya", "Teman Dekat", "Kontak Terpilih"};
        new AlertDialog.Builder(this)
            .setTitle("Siapa yang bisa lihat?")
            .setItems(options, (dialog, which) -> {
                String privacy = "everyone";
                if (which == 1) privacy = "contacts";
                else if (which == 2) privacy = "close_friends";
                else if (which == 3) privacy = "selected";

                simpanStory(privacy, selectedMediaUri.toString(), etStoryCaption.getText().toString());
            })
            .show();
    }

    private void simpanStory(String privacy, String mediaUrl, String caption) {
        String myUid = FirebaseAuth.getInstance().getUid();
        DatabaseReference mDb = FirebaseDatabase.getInstance().getReference();
        String storyId = mDb.child("stories").child(myUid).push().getKey();

        Map<String, Object> storyData = new HashMap<>();
        storyData.put("name", "Status saya");
        storyData.put("timestamp", System.currentTimeMillis());
        storyData.put("privacy", privacy);
        storyData.put("imageUrl", mediaUrl);
        storyData.put("caption", caption);

        if (storyId != null) {
            mDb.child("stories").child(myUid).child(storyId).setValue(storyData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Story Berhasil Diposting!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
