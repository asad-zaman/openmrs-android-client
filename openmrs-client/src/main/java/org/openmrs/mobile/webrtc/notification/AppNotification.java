package org.openmrs.mobile.webrtc.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import org.openmrs.mobile.R;

import java.util.Random;


public class AppNotification {
    private AppNotification() {

    }

    private PendingIntent pendingIntent;
    private String title;
    private String message;

    private int notificationId = 100100;

    public void sendNotification(Context context) {
        String channelId = "CHANNEL_ID";

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                // temp icon set instead of ic_launcher
                .setSmallIcon(R.drawable.ic_openmrs)
                //.setContentTitle("Firebase Push Notification")
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        /*NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);*/

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default Channel";
            String description = "Default Channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }


//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
        int notId = new Random().nextInt(10000);
        notificationManager.notify(notId, notificationBuilder.build());
    }


    public static class Builder {
        private Context context;
        private AppNotification appNotification;

        public Builder(Context context) {
            this.context = context;
            appNotification = new AppNotification();
        }

        public Builder title(String title) {
            appNotification.title = title;
            return this;
        }

        public Builder body(String body) {
            appNotification.message = body;
            return this;
        }

        public Builder pendingIntent(PendingIntent pendingIntent) {
            appNotification.pendingIntent = pendingIntent;
            return this;
        }

        public void send() {
            appNotification.sendNotification(context);
        }
    }
}
