package app.neont.api;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class NewMessageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        ImageView btnBack = findViewById(R.id.btnBackNew);
        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.rowNewGroup).setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateGroupActivity.class);
            intent.putExtra("type", "group");
            startActivity(intent);
        });

        findViewById(R.id.rowNewChannel).setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateGroupActivity.class);
            intent.putExtra("type", "channel");
            startActivity(intent);
        });
    }
}
