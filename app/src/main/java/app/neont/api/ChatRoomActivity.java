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

        if (senderId != null && receiverId != null) {
            if (senderId.compareTo(receiverId) < 0) roomId = senderId + "_" + receiverId;
            else roomId = receiverId + "_" + senderId;
        } else { finish(); return; }

        // Klik Header buka Profil
        findViewById(R.id.headerChat).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, UserProfileActivity.class).putExtra("uid", receiverId));
        });

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);
        
        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!msg.isEmpty()) kirimPesan(msg);
        });

        // Typing Indicator
        etMessage.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                mDb.child("typing").child(roomId).child(senderId).setValue(s.length() > 0);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        
        loadPesan();
        listenTyping();
    }

    private void kirimPesan(String text) {
        // SANITASI: Batasi 4000 karakter & buang karakter kontrol
        String cleanText = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").substring(0, Math.min(text.length(), 4000));

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", senderId);
        messageData.put("receiverId", receiverId);
        messageData.put("text", cleanText);
        messageData.put("timestamp", System.currentTimeMillis());
        messageData.put("status", "sent");
        
        mDb.child("chats").child(roomId).push().setValue(messageData)
            .addOnSuccessListener(aVoid -> etMessage.setText(""));
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
                tvChatStatus.setText(isTyping ? "sedang mengetik..." : "Online");
            }
            @Override public void onCancelled(DatabaseError error) {}
        };
        mDb.child("typing").child(roomId).addValueEventListener(typingListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDb != null && messageListener != null) mDb.child("chats").child(roomId).removeEventListener(messageListener);
        if (mDb != null && typingListener != null) mDb.child("typing").child(roomId).removeEventListener(typingListener);
        mDb.child("typing").child(roomId).child(senderId).setValue(false);
    }
}
