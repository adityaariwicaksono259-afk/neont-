package app.neont.api;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
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
    private TextInputEditText etMessage;
    private ImageView btnSend;
    private DatabaseReference mDb;
    private String senderId, receiverId, receiverName, roomId;
    private List<MessageModel> messageList;
    private MessageAdapter adapter;
    private ValueEventListener messageListener, typingListener;
    private TextView tvChatName, tvChatStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvChatName = findViewById(R.id.tvChatName);
        tvChatStatus = findViewById(R.id.tvChatStatus);

        mDb = FirebaseDatabase.getInstance().getReference();
        senderId = FirebaseAuth.getInstance().getUid();
        
        receiverId = getIntent().getStringExtra("uid");
        receiverName = getIntent().getStringExtra("name");
        tvChatName.setText(receiverName != null ? receiverName : "Chat");

        if ("GLOBAL".equals(receiverId)) roomId = "GLOBAL";
        else {
            if (senderId.compareTo(receiverId) < 0) roomId = senderId + "_" + receiverId;
            else roomId = receiverId + "_" + senderId;
        }

        messageList = new ArrayList<>();
        
        // Setup Adapter dengan Long Press Listener
        adapter = new MessageAdapter(messageList, message -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pesan: " + message.getText());
            builder.setItems(new CharSequence[]{"Balas (Reply)", "Hapus", "Salin (Copy)"}, (dialog, which) -> {
                if (which == 0) {
                    etMessage.setText(""); // Nanti kita isi dengan quote
                    etMessage.setHint("Membalas: " + message.getText());
                } else if (which == 1) {
                    mDb.child("chats").child(roomId).child(message.getMessageId()).removeValue();
                } else if (which == 2) {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Pesan", message.getText());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Disalin!", Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        });
        
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);
        
        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                kirimPesan(msg);
            }
        });

        // Typing Indicator
        etMessage.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) mDb.child("typing").child(roomId).child(senderId).setValue(true);
                else mDb.child("typing").child(roomId).child(senderId).setValue(false);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        
        loadPesan();
        listenTyping();
        listenOnlineStatus();
    }

    private void kirimPesan(String text) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", senderId);
        messageData.put("receiverId", receiverId);
        messageData.put("text", text);
        messageData.put("timestamp", System.currentTimeMillis());
        messageData.put("status", "sent");
        
        mDb.child("chats").child(roomId).push().setValue(messageData)
            .addOnSuccessListener(aVoid -> {
                etMessage.setText("");
                etMessage.setHint("Pesan");
            });
    }

    private void loadPesan() {
        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String sender = data.child("senderId").getValue(String.class);
                    String text = data.child("text").getValue(String.class);
                    Long timestamp = data.child("timestamp").getValue(Long.class);
                    String status = data.child("status").getValue(String.class);

                    if (text != null && sender != null) {
                        messageList.add(new MessageModel(data.getKey(), sender, receiverId, text, 
                                timestamp != null ? timestamp : 0, status != null ? status : "sent"));
                        if (!sender.equals(senderId)) data.getRef().child("status").setValue("read");
                    }
                }
                adapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) rvMessages.scrollToPosition(messageList.size() - 1);
            }
            @Override public void onCancelled(DatabaseError error) {}
        };
        mDb.child("chats").child(roomId).orderByChild("timestamp").addValueEventListener(messageListener);
    }

    private void listenTyping() {
        typingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean isTyping = false;
                for (DataSnapshot data : snapshot.getChildren()) {
                    String uid = data.getKey();
                    Boolean typing = data.getValue(Boolean.class);
                    if (uid != null && !uid.equals(senderId) && typing != null && typing) isTyping = true;
                }
                if (isTyping) tvChatStatus.setText("sedang mengetik...");
                else tvChatStatus.setText("Online"); // Sementara
            }
            @Override public void onCancelled(DatabaseError error) {}
        };
        mDb.child("typing").child(roomId).addValueEventListener(typingListener);
    }

    private void listenOnlineStatus() {
        // Logika Online Status sementara
        if (receiverId != null && !"GLOBAL".equals(receiverId)) {
            mDb.child("users").child(receiverId).child("status").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String status = snapshot.getValue(String.class);
                    if (status != null) tvChatStatus.setText(status.equals("online") ? "Online" : "Terakhir dilihat baru-baru ini");
                }
                @Override public void onCancelled(DatabaseError error) {}
            });
        } else {
            tvChatStatus.setText("Publik");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDb != null && messageListener != null) mDb.child("chats").child(roomId).removeEventListener(messageListener);
        if (mDb != null && typingListener != null) mDb.child("typing").child(roomId).removeEventListener(typingListener);
        mDb.child("typing").child(roomId).child(senderId).setValue(false);
    }
}
