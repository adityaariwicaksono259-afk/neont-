package app.neont.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Context context;

    public GlobalExceptionHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // Tampilkan error di layar pakai Toast (biar lo bisa screenshot)
        new Handler(Looper.getMainLooper()).post(() -> {
            String errorMsg = throwable.toString();
            Toast.makeText(context, "CRASH: " + errorMsg, Toast.LENGTH_LONG).show();
        });

        // Print ke Logcat (kalau nanti lo pasang adb)
        Log.e("NEONT_CRASH", "Error: ", throwable);
        
        // Biarin aplikasi mati (jangan di-restart biar nggak looping)
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
