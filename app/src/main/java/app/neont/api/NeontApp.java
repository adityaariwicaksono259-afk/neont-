package app.neont.api;

import android.app.Application;
import androidx.emoji2.text.DefaultEmojiCompatConfig;
import androidx.emoji2.text.EmojiCompat;

public class NeontApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Aktifkan Offline Persistence (Firebase)
        // Ini memungkinkan chat dibaca saat offline, dan dikirim saat online
        com.google.firebase.database.FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        

        // Tangkap error global (CCTV) - untuk debugging
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            android.util.Log.e("NEONT_CRASH", "Fatal: " + throwable.getMessage(), throwable);
            throwable.printStackTrace();
            System.exit(1);
        });

        // Inisialisasi Emoji2 (Standar Android, stabil)
        EmojiCompat.Config config = DefaultEmojiCompatConfig.create(this);
        if (config != null) {
            config.setReplaceAll(true);
            EmojiCompat.init(config);
        }
    }
}
