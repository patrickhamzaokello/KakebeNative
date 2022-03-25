package com.pkasemer.kakebeshoplira;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushNotificationService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String title = remoteMessage.getNotification().getTitle();
        String text = remoteMessage.getNotification().getBody();

        final String CHANNEL_ID = "HEADS_UP_NOTIFICATION";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Heads Up Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );

            Notification.Builder notification =
                    new Notification.Builder(this, CHANNEL_ID)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setSmallIcon(R.drawable.kakebelogo)
                            .setAutoCancel(true);

            NotificationManagerCompat.from(this).notify(1, notification.build());

        }
        super.onMessageReceived(remoteMessage);


    }
}
