package app.neont.api;

import android.content.Intent;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import org.json.JSONObject;
import com.bumptech.glide.Glide;

public class UserProfileActivity extends AppCompatActivity {
    private TextView tvName, tvUsername, tvStatus;
    private ImageView ivPhoto;
    private String targetUid, myUid;
    private boolean isSelf = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        tvName = findViewById(R.id.tvUserName);
        tvUsername = findViewById(R.id.tvUserUsername);
        tvStatus = findViewById(R.id.tvStatus);
        ivPhoto = findViewById(R.id.ivUserPhoto);
        myUid = FirebaseAuth.getInstance().getUid();
        targetUid = getIntent().getStringExtra("uid");

        if (targetUid == null) targetUid = myUid;
        if (targetUid == null) { finish(); return; }

        isSelf = targetUid.equals(myUid);

        // Ambil data REAL dari D1
        D1Helper.getUser(targetUid, new D1Helper.UserCallback() {
            @Override
            public void onSuccess(JSONObject user) {
                String name = user.optString("display_name", "User");
                String username = user.optString("username", "");
                String bio = user.optString("bio", "");
                String photoUrl = user.optString("photo_url", "");
                String privacyPhoto = user.optString("privacy_profile_photo", "everybody");

                tvName.setText(name.isEmpty() ? "User" : name);
                tvUsername.setText(username.isEmpty() ? "@user" : "@" + username);
                
                if (!bio.isEmpty() && tvStatus != null) {
                    tvStatus.setText(bio);
                }
                
                // Privasi & Foto
                boolean canSeePhoto = true;
                if (!isSelf && "nobody".equals(privacyPhoto)) {
                    canSeePhoto = false;
                }
                
                if (canSeePhoto && !photoUrl.isEmpty() && ivPhoto != null) {
                    com.bumptech.glide.Glide.with(UserProfileActivity.this).load(photoUrl).into(ivPhoto);
                } else if (ivPhoto != null) {
                    ivPhoto.setImageResource(android.R.drawable.ic_menu_myplaces);
                }
            }
            @Override public void onError(String message) {}
        });

        // Setup Tombol Aksi (Self vs Other)
        View layoutSelf = findViewById(R.id.layoutSelfActions);
        View layoutSettings = findViewById(R.id.layoutSettingsList);
        Button btnMessage = findViewById(R.id.btnMessageUser);

        if (isSelf) {
            layoutSelf.setVisibility(View.VISIBLE);
            layoutSettings.setVisibility(View.VISIBLE);
            btnMessage.setVisibility(View.GONE);

            findViewById(R.id.btnSetPhoto).setOnClickListener(v -> {
            String[] options = {"Kamera", "Galeri"};
            new android.app.AlertDialog.Builder(this)
                .setTitle("Pasang Foto Profil")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Toast.makeText(this, "Fitur Kamera segera hadir", Toast.LENGTH_SHORT).show();
                    } else {
                        // Buka galeri (Butuh Activity Result API, sementara kita pakai Intent sederhana)
                        // Minta izin galeri dulu (Android 13+)
                        if (android.os.Build.VERSION.SDK_INT >= 33) {
                            if (androidx.core.content.ContextCompat.checkSelfPermission(UserProfileActivity.this, android.Manifest.permission.READ_MEDIA_IMAGES) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 200);
                            } else {
                                openGallery();
                            }
                        } else {
                            openGallery();
                        }
                    }
                }).show();
        });
            findViewById(R.id.btnEditInfo).setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
            findViewById(R.id.btnSettings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
            findViewById(R.id.rowEditProfile).setOnClickListener(v -> Toast.makeText(this, "Fitur Edit Profil", Toast.LENGTH_SHORT).show());
        } else {
            layoutSelf.setVisibility(View.GONE);
            layoutSettings.setVisibility(View.GONE);
            btnMessage.setVisibility(View.VISIBLE);
            btnMessage.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatRoomActivity.class);
                intent.putExtra("uid", targetUid);
                intent.putExtra("name", tvName.getText().toString());
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            android.net.Uri selectedImage = data.getData();
            if (selectedImage != null) {
                // Ubah Uri jadi File
                java.io.File file = new java.io.File(getCacheDir(), "avatar.jpg");
                try (java.io.InputStream input = getContentResolver().openInputStream(selectedImage);
                     java.io.FileOutputStream output = new java.io.FileOutputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = input.read(buffer)) > 0) output.write(buffer, 0, len);
                } catch (Exception e) { e.printStackTrace(); }

                // Upload ke Filebase
                UploadHelper.uploadAvatar(file, new UploadHelper.UploadCallback() {
                    @Override public void onSuccess(String publicUrl) {
                        // Simpan URL ke D1
                        try {
                            org.json.JSONObject update = new org.json.JSONObject();
                            update.put("photo_url", publicUrl);
                            D1Helper.updateUser(myUid, update, new D1Helper.UserCallback() {
                                @Override public void onSuccess(org.json.JSONObject user) {
                                    Toast.makeText(UserProfileActivity.this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show();
                                }
                                @Override public void onError(String message) {
                                    Toast.makeText(UserProfileActivity.this, "Gagal simpan: " + message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                    @Override public void onError(String message) {
                        Toast.makeText(UserProfileActivity.this, "Upload gagal: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void openGallery() {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else if (requestCode == 200) {
            Toast.makeText(this, "Izin galeri ditolak", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data profil dari D1 saat kembali dari Edit
        if (targetUid != null) {
            D1Helper.getUser(targetUid, new D1Helper.UserCallback() {
                @Override public void onSuccess(org.json.JSONObject user) {
                    String name = user.optString("display_name", "User");
                    String username = user.optString("username", "");
                    tvName.setText(name.isEmpty() ? "User" : name);
                    tvUsername.setText(username.isEmpty() ? "@user" : "@" + username);
                }
                @Override public void onError(String message) {}
            });
        }
    }
}
