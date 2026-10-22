package app.neont.api;

import android.app.Application;
import androidx.emoji2.text.DefaultEmojiCompatConfig;
import androidx.emoji2.text.EmojiCompat;

public class NeontApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Inisialisasi Emoji2 (Download font emoji terbaru)
        // DefaultEmojiCompatConfig.create() mengembalikan config, bukan constructor
        EmojiCompat.Config config = DefaultEmojiCompatConfig.create(this);
        if (config != null) {
            config.setReplaceAll(true);
            EmojiCompat.init(config);
        }
    }
}
