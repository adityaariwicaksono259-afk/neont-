package app.neont.api;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageView btnSend;
    private LinearLayout layoutInput;
    private DatabaseReference mDb;
    private String senderId, receiverId, receiverName, roomId, chatType = "private";
    private List<MessageModel> messageList;
    private MessageAdapter adapter;
    private ValueEventListener messageListener;
    private TextView tvChatName, tvChatStatus;

    // Path dasar: chats/{roomId}/messages
    private DatabaseReference messagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        layoutInput = findViewById(R.id.bottomInput);
        tvChatName = findViewById(R.id.tvChatName);
        tvChatStatus = findViewById(R.id.tvChatStatus);

        mDb = FirebaseDatabase.getInstance().getReference();
        senderId = FirebaseAuth.getInstance().getUid();
        receiverId = getIntent().getStringExtra("uid");
        String groupId = getIntent().getStringExtra("groupId");
        if (groupId != null) { roomId = "group_" + groupId; receiverId = groupId; }
        receiverName = getIntent().getStringExtra("name");
        if (groupId != null) {
            // Ambil nama grup dari Firebase
            mDb.child("groups").child(groupId).child("title").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot s) {
                    String title = s.getValue(String.class);
                    if (title != null) tvChatName.setText(title);
                }
                @Override public void onCancelled(DatabaseError e) {}
            });
        } else {
            tvChatName.setText(receiverName != null ? receiverName : "Chat");
        }

        if (senderId != null && receiverId != null && groupId == null) {
            if (senderId.compareTo(receiverId) < 0) roomId = senderId + "_" + receiverId;
            else roomId = receiverId + "_" + senderId;
        } else if (roomId == null) { finish(); return; }

        // ✅ FIX: path sesuai firebase_rules.json
        messagesRef = mDb.child("chats").child(roomId).child("messages");

        findViewById(R.id.headerChat).setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra("uid", receiverId);
            startActivity(intent);
        });

        // Cek tipe chat (private/group/channel)
        mDb.child("groups").child(receiverId).child("type").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                String type = snapshot.getValue(String.class);
                if (type != null) chatType = type;
                if ("channel".equals(chatType)) {
                    layoutInput.setVisibility(LinearLayout.GONE);
                    tvChatStatus.setText("Channel");
                } else {
                    tvChatStatus.setText("Grup");
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList, message -> showMessageMenu(message), (message, emoji) -> {
            // Toggle Reaction
            String uid = senderId;
            com.google.firebase.database.DatabaseReference ref = messagesRef.child(message.getMessageId()).child("reactions").child(emoji).child(uid);
            ref.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                    if (snapshot.exists()) ref.removeValue();
                    else ref.setValue(true);
                }
                @Override public void onCancelled(com.google.firebase.database.DatabaseError error) {}
            });
        });
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                String replyTo = (String) etMessage.getTag();
                kirimPesan(msg, replyTo);
            }
        });

        loadPesan();
    }

    private void showMessageMenu(MessageModel message) {
        String[] options = {"Balas (Reply)", "Reaction ❤️", "Reaction 👍", "Salin (Copy)", "Teruskan", "Ganti Wallpaper", "Hapus"};
        new AlertDialog.Builder(this)
            .setTitle("Pesan")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    etMessage.setHint("Membalas: " + message.getText());
                    etMessage.setTag(message.getMessageId());
                } else if (which == 1) {
                    messagesRef.child(message.getMessageId()).child("reactions").child("❤️").child(senderId).setValue(true);
                } else if (which == 2) {
                    messagesRef.child(message.getMessageId()).child("reactions").child("👍").child(senderId).setValue(true);
                } else if (which == 3) {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Pesan", message.getText());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Disalin!", Toast.LENGTH_SHORT).show();
                } else if (which == 4) {
                    // Forward (Teruskan)
                    Toast.makeText(this, "Pilih chat tujuan (segera hadir)", Toast.LENGTH_SHORT).show();
                } else if (which == 5) {
                    // Ganti Wallpaper
                    showWallpaperDialog();
                } else if (which == 6) {
                    messagesRef.child(message.getMessageId()).removeValue();
                }
            })
            .show();
    }

    /**
     * Tiru logika ChatActivity Telegram:
     * - Optimistic UI: bubble muncul dulu dengan status SENDING
     * - Firebase push → status jadi SENT
     */
    private void kirimPesan(String text, String replyToId) {
        String tempKey = messagesRef.push().getKey();
        if (tempKey == null) return;

        MessageModel optimistic = new MessageModel(tempKey, senderId, receiverId, text,
                System.currentTimeMillis(), "SENDING", replyToId, null, null);
        messageList.add(optimistic);
        adapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.scrollToPosition(messageList.size() - 1);
        etMessage.setText("");
        etMessage.setHint("Pesan");
        etMessage.setTag(null);

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", senderId);
        messageData.put("receiverId", receiverId);
        String encryptedText = CryptoHelper.encrypt(text, roomId);
        messageData.put("text", encryptedText);
        messageData.put("timestamp", ServerValue.TIMESTAMP);
        messageData.put("status", "sent");
        if (replyToId != null) messageData.put("replyToMsgId", replyToId);

        messagesRef.child(tempKey).setValue(messageData)
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Gagal kirim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void loadPesan() {
        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String sender = data.child("senderId").getValue(String.class);
                    String encryptedText = data.child("text").getValue(String.class);
                    String text = (encryptedText != null) ? CryptoHelper.decrypt(encryptedText, roomId) : "";
                    Long timestamp = data.child("timestamp").getValue(Long.class);
                    String status = data.child("status").getValue(String.class);
                    String replyTo = data.child("replyToMsgId").getValue(String.class);
                    String mediaUrl = data.child("mediaUrl").getValue(String.class);
                    Map<String, Map<String, Boolean>> reactions = new HashMap<>();
                    for (DataSnapshot reaction : data.child("reactions").getChildren()) {
                        Map<String, Boolean> users = new HashMap<>();
                        for (DataSnapshot user : reaction.getChildren()) users.put(user.getKey(), true);
                        reactions.put(reaction.getKey(), users);
                    }
                    if (text != null && sender != null) {
                        messageList.add(new MessageModel(data.getKey(), sender, receiverId, text,
                                timestamp != null ? timestamp : 0,
                                status != null ? status : "sent",
                                replyTo, mediaUrl, reactions));
                        // Tandai "read" kalau pesan orang lain dibuka
                        if (!sender.equals(senderId) && (status == null || !"read".equals(status))) {
                            data.getRef().child("status").setValue("read");
                            
                            // Munculkan Notifikasi (jika bukan dari diri sendiri)
                            if (!isFinishing() && !sender.equals(senderId)) {
                                String senderName = data.child("senderName").getValue(String.class);
                                NotificationHelper.showNotification(ChatRoomActivity.this, 
                                    senderName != null ? senderName : "Pesan Baru", 
                                    text, roomId, senderName != null ? senderName : "User");
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) rvMessages.scrollToPosition(messageList.size() - 1);
            }
            @Override public void onCancelled(DatabaseError error) {}
        };
        messagesRef.orderByChild("timestamp").addValueEventListener(messageListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesRef != null && messageListener != null) messagesRef.removeEventListener(messageListener);
    }

    private void showWallpaperDialog() {
        String[] colors = {"Default", "Hijau", "Biru", "Ungu", "Gelap"};
        new AlertDialog.Builder(this)
            .setTitle("Pilih Wallpaper")
            .setItems(colors, (dialog, which) -> {
                int color = 0xFFFFFFFF; // Default
                if (which == 1) color = 0xFFE8F5E9; // Hijau
                else if (which == 2) color = 0xFFE3F2FD; // Biru
                else if (which == 3) color = 0xFFF3E5F5; // Ungu
                else if (which == 4) color = 0xFF212121; // Gelap
                
                View root = findViewById(android.R.id.content);
                root.setBackgroundColor(color);
                Toast.makeText(this, "Wallpaper diganti", Toast.LENGTH_SHORT).show();
            }).show();
    }
}
