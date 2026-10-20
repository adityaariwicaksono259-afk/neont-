package app.neont.api;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatRoomActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        
        TextView tvChatName = findViewById(R.id.tvChatName);
        tvChatName.setText("CEK FIREBASE...");

        String myUid = FirebaseAuth.getInstance().getUid();
        if (myUid == null) {
            tvChatName.setText("ERROR: LO BELUM LOGIN!");
            return;
        }

        DatabaseReference mDb = FirebaseDatabase.getInstance().getReference();
        
        // Coba baca node "/chats" doang
        mDb.child("chats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Kalau berhasil baca, berarti koneksi AMAN
                tvChatName.setText("FIREBASE AMAN! ADA " + snapshot.getChildrenCount() + " CHAT");
                Toast.makeText(ChatRoomActivity.this, "Koneksi Database Sukses!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Kalau Firebase nolak, MUNCULIN ERROR ASLINYA DI LAYAR
                new AlertDialog.Builder(ChatRoomActivity.this)
                    .setTitle("FIREBASE ERROR")
                    .setMessage("Kode: " + error.getCode() + "\nPesan: " + error.getMessage())
                    .setPositiveButton("OK", (d, w) -> finish())
                    .show();
            }
        });
    }
}
