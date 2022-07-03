package com.shop.kakebe.KaKebe;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.shop.kakebe.KaKebe.HelperClasses.SharedPrefManager;
import com.shop.kakebe.KaKebe.Models.User;
import com.shop.kakebe.KaKebe.R;

public class PushNotificationService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String title = remoteMessage.getNotification().getTitle();
            String text = remoteMessage.getNotification().getBody();

            final String CHANNEL_ID = "HEADS_UP_NOTIFICATION";

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Heads Up Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );

            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            User user = SharedPrefManager.getInstance(this).getUser();

            Notification.Builder notification =
                    new Notification.Builder(this, CHANNEL_ID)
                            .setContentTitle(title)
                            .setContentText(user.getFullname()+", "+ text)
                            .setStyle(new Notification.BigTextStyle()
                                    .bigText(user.getFullname()+ ", "+ text))
                            .setSmallIcon(R.drawable.kakebelogo)
                            .setAutoCancel(true);

            NotificationManagerCompat.from(this).notify(1, notification.build());

        }

        super.onMessageReceived(remoteMessage);



    }
}
