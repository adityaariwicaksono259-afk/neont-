package app.neont.api;

import android.app.Application;
import androidx.emoji2.text.EmojiCompat;
import androidx.emoji2.text.DefaultEmojiCompatConfig;

public class NeontApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Inisialisasi Emoji2 (Download font emoji terbaru)
        EmojiCompat.Config config = new DefaultEmojiCompatConfig(this);
        config.setReplaceAll(true);
        EmojiCompat.init(config);
    }
}
