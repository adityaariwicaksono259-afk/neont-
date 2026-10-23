package app.neont.api;

import android.app.Application;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import androidx.emoji2.text.DefaultEmojiCompatConfig;
import androidx.emoji2.text.EmojiCompat;

public class NeontApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // CCTV: Tangkap error global dan tampilkan di layar
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            final String errorMsg = throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
            
            // Tampilkan di layar (menggunakan Activity baru)
            Intent intent = new Intent(this, CrashActivity.class);
            intent.putExtra("error", errorMsg);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Matikan proses
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        });

        // Inisialisasi Emoji2
        EmojiCompat.Config config = DefaultEmojiCompatConfig.create(this);
        if (config != null) {
            config.setReplaceAll(true);
            EmojiCompat.init(config);
        }
    }
}
