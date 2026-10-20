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
    private List<MessageModel> messageList;
    private MessageAdapter adapter;
    private ValueEventListener messageListener;

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
        
        receiverId = getIntent().getStringExtra("uid");
        receiverName = getIntent().getStringExtra("name");
        
        if (senderId == null) {
            Toast.makeText(this, "Sesi habis, login ulang", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvChatName.setText(receiverName != null ? receiverName : "Chat");

        if ("GLOBAL".equals(receiverId)) {
            roomId = "GLOBAL";
        } else if (receiverId != null) {
            if (senderId.compareTo(receiverId) < 0) {
                roomId = senderId + "_" + receiverId;
            } else {
                roomId = receiverId + "_" + senderId;
            }
        } else {
            Toast.makeText(this, "Error: User ID kosong", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);
        
        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                kirimPesan(msg);
            }
        });
        
        loadPesanAman();
    }

    private void kirimPesan(String text) {
        long timestamp = System.currentTimeMillis();
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", senderId);
        messageData.put("receiverId", receiverId);
        messageData.put("text", text);
        messageData.put("timestamp", timestamp);
        messageData.put("status", "sent");
        
        mDb.child("chats").child(roomId).push().setValue(messageData)
            .addOnSuccessListener(aVoid -> etMessage.setText(""))
            .addOnFailureListener(e -> Toast.makeText(this, "Gagal kirim: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // FUNGSI BARU: Baca data manual (Anti Crash)
    private void loadPesanAman() {
        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    messageList.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        // Baca manual satu-satu, jangan pakai Model Class
                        String id = data.getKey();
                        String sender = data.child("senderId").getValue(String.class);
                        String text = data.child("text").getValue(String.class);
                        Long timestamp = data.child("timestamp").getValue(Long.class);
                        String status = data.child("status").getValue(String.class);

                        if (text != null && sender != null) {
                            MessageModel msg = new MessageModel(
                                id, sender, receiverId, text, 
                                timestamp != null ? timestamp : 0, 
                                status != null ? status : "sent"
                            );
                            messageList.add(msg);

                            // Update read kalau dari orang lain
                            if (!sender.equals(senderId)) {
                                data.getRef().child("status").setValue("read");
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (!messageList.isEmpty()) {
                        rvMessages.scrollToPosition(messageList.size() - 1);
                    }
                } catch (Exception e) {
                    // Kalau error parsing, munculin Toast, jangan biarin crash
                    Toast.makeText(ChatRoomActivity.this, "Parse Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ChatRoomActivity.this, "DB Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        
        mDb.child("chats").child(roomId).orderByChild("timestamp").addValueEventListener(messageListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDb != null && messageListener != null) {
            mDb.child("chats").child(roomId).removeEventListener(messageListener);
        }
    }
}
