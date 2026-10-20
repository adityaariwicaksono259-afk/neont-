package app.neont.api;

import android.app.AlertDialog;
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
        Thread.setDefaultUncaughtExceptionHandler(new ErrorHandler(this));
        
        // CCTV: Bungkus semua kode pake try-catch
        try {
            setContentView(R.layout.activity_chat_room);

            rvMessages = findViewById(R.id.rvMessages);
            etMessage = findViewById(R.id.etMessage);
            btnSend = findViewById(R.id.btnSend);
            TextView tvChatName = findViewById(R.id.tvChatName);

            mDb = FirebaseDatabase.getInstance().getReference();
            senderId = FirebaseAuth.getInstance().getUid();
            
            receiverId = getIntent().getStringExtra("uid");
            receiverName = getIntent().getStringExtra("name");

            tvChatName.setText(receiverName != null ? receiverName : "Chat");

            if ("GLOBAL".equals(receiverId)) {
                roomId = "GLOBAL";
            } else {
                if (senderId.compareTo(receiverId) < 0) {
                    roomId = senderId + "_" + receiverId;
                } else {
                    roomId = receiverId + "_" + senderId;
                }
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
            
            loadPesan();
            
        } catch (Exception e) {
            // TANGKAP ERROR DAN TAMPILKAN DI LAYAR
            new AlertDialog.Builder(this)
                .setTitle("ERROR DI CHAT ROOM")
                .setMessage(e.toString() + "\n\n" + getStackTrace(e))
                .setPositiveButton("OK", (d, w) -> finish())
                .show();
        }
    }

    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    private void kirimPesan(String text) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", senderId);
        messageData.put("receiverId", receiverId);
        messageData.put("text", text);
        messageData.put("timestamp", System.currentTimeMillis());
        messageData.put("status", "sent");
        
        mDb.child("chats").child(roomId).push().setValue(messageData)
            .addOnSuccessListener(aVoid -> etMessage.setText(""))
            .addOnFailureListener(e -> Toast.makeText(this, "Gagal kirim: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadPesan() {
        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    messageList.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        String sender = data.child("senderId").getValue(String.class);
                        String text = data.child("text").getValue(String.class);
                        Long timestamp = data.child("timestamp").getValue(Long.class);
                        String status = data.child("status").getValue(String.class);

                        if (text != null && sender != null) {
                            messageList.add(new MessageModel(data.getKey(), sender, receiverId, text, 
                                    timestamp != null ? timestamp : 0, status != null ? status : "sent"));
                            
                            if (!sender.equals(senderId)) {
                                data.getRef().child("status").setValue("read");
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (!messageList.isEmpty()) rvMessages.scrollToPosition(messageList.size() - 1);
                } catch (Exception e) {
                    new AlertDialog.Builder(ChatRoomActivity.this)
                        .setTitle("ERROR PARSING DATA")
                        .setMessage(e.toString())
                        .setPositiveButton("OK", null)
                        .show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                new AlertDialog.Builder(ChatRoomActivity.this)
                    .setTitle("ERROR DATABASE")
                    .setMessage("Kode: " + error.getCode() + "\nPesan: " + error.getMessage())
                    .setPositiveButton("OK", (d, w) -> finish())
                    .show();
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
