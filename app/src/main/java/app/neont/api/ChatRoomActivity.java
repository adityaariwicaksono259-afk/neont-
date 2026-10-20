package app.neont.api;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;
    private DatabaseReference mDb;
    private String senderId, receiverId, receiverName, roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        TextView tvChatName = findViewById(R.id.tvChatName);

        mDb = FirebaseDatabase.getInstance().getReference();
        senderId = FirebaseAuth.getInstance().getUid();
        
        // Ambil data dari Intent (dikirim dari HomeActivity)
        receiverId = getIntent().getStringExtra("uid");
        receiverName = getIntent().getStringExtra("name");
        tvChatName.setText(receiverName != null ? receiverName : "Chat");

        // Bikin Room ID unik (urutkan biar konsisten)
        if (senderId != null && receiverId != null) {
            if (senderId.compareTo(receiverId) < 0) {
                roomId = senderId + "_" + receiverId;
            } else {
                roomId = receiverId + "_" + senderId;
            }
        } else {
            Toast.makeText(this, "Gagal memuat chat: User ID kosong", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tampilan List Pesan (belum ada adapter, kita siapin tempatnya)
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        
        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                kirimPesan(msg);
            }
        });
        
        // Load pesan realtime
        loadPesan();
    }

    private void kirimPesan(String text) {
        long timestamp = System.currentTimeMillis();
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", senderId);
        messageData.put("receiverId", receiverId);
        messageData.put("text", text);
        messageData.put("timestamp", timestamp);
        
        mDb.child("chats").child(roomId).push().setValue(messageData)
            .addOnSuccessListener(aVoid -> {
                etMessage.setText(""); // Bersihin kolom
            })
            .addOnFailureListener(e -> Toast.makeText(ChatRoomActivity.this, "Gagal kirim: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadPesan() {
        // Placeholder: nanti kita bikin MessageAdapter buat nampilin bubble chat
        mDb.child("chats").child(roomId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // TODO: Tampilkan chat ke RecyclerView
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ChatRoomActivity.this, "Error load pesan: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
