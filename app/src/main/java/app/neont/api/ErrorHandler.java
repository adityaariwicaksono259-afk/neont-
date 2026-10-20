package app.neont.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ErrorHandler implements Thread.UncaughtExceptionHandler {
    private Context context;
    private Thread.UncaughtExceptionHandler defaultHandler;

    public ErrorHandler(Context context) {
        this.context = context;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // Tampilkan error di UI Thread
        new Handler(Looper.getMainLooper()).post(() -> {
            String errorMsg = throwable.toString();
            // Tampilin Toast panjang biar kebaca
            Toast.makeText(context, "CRASH DITANGKAP: " + errorMsg, Toast.LENGTH_LONG).show();
        });

        // Biarin proses berjalan (jangan di-kill) biar Toast-nya kebaca
        try {
            Thread.sleep(2000); // Tunggu 2 detik biar Toast kebaca
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Baru matiin
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
