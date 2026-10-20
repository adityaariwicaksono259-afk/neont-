package app.neont.api;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ChatRoomActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Kita cuma nge-set layout, tanpa nyentuh Firebase sama sekali
        setContentView(R.layout.activity_chat_room);
        
        // Coba ganti teks header buat mastiin layout-nya kebaca
        TextView tvChatName = findViewById(R.id.tvChatName);
        if (tvChatName != null) {
            tvChatName.setText("TEST: LAYOUT JALAN");
        }
    }
}
