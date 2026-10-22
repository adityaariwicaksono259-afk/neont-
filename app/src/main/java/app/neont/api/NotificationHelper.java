package app.neont.api;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "neont_chat";
    private static final String CHANNEL_NAME = "Chat Messages";

    public static void showNotification(Context ctx, String title, String message, String roomId, String senderName) {
        // 1. Cek preferensi (apakah user blokir notifikasi?)
        SharedPreferences prefs = ctx.getSharedPreferences("neont_settings", Context.MODE_PRIVATE);
        String notifMode = prefs.getString("notif_mode", "all"); // Default: all

        if ("blocked".equals(notifMode)) {
            return; // Jangan tampilkan notifikasi
        }

        boolean isSilent = "silent".equals(notifMode);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        // 2. Buat Channel (Wajib Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            if (isSilent) channel.setSound(null, null);
            nm.createNotificationChannel(channel);
        }

        // 3. Intent saat notifikasi diklik (buka ChatRoom)
        Intent intent = new Intent(ctx, ChatRoomActivity.class);
        intent.putExtra("uid", roomId);
        intent.putExtra("name", senderName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // 4. Build & Show
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(isSilent ? NotificationCompat.PRIORITY_LOW : NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (isSilent) {
            builder.setSound(null);
            builder.setVibrate(null);
        }

        int notifId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        nm.notify(notifId, builder.build());
    }
}
