package com.shop.kakebe.KaKebe;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.shop.kakebe.KaKebe.HelperClasses.SharedPrefManager;
import com.shop.kakebe.KaKebe.Models.User;


public class OrderCompleted extends AppCompatActivity  {

    private static final String CHANNEL_ID = "orderPlaced";
    private static final String NAME = "Placed Order";
    private static final int NOTIFICATIONID = 3;
    NotificationManagerCompat notificationManagerCompat;
    Notification notification;
    Button btnTodaysMEnu,btnGoHOme;
    ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_completed);
        actionBar = getSupportActionBar(); // or getActionBar();
        actionBar.setTitle(R.string.OrderTitle);

        initNotification();
        notificationManagerCompat.notify(NOTIFICATIONID, notification);

        btnTodaysMEnu = findViewById(R.id.order_btn_todayMenu);
        btnGoHOme = findViewById(R.id.btnGoHOme);


        btnTodaysMEnu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(OrderCompleted.this, ManageOrders.class);
                startActivity(i);
            }
        });

        btnGoHOme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(OrderCompleted.this, RootActivity.class);
                startActivity(i);
            }
        });



    }

    private void initNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, ManageOrders.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        User user = SharedPrefManager.getInstance(OrderCompleted.this).getUser();

        NotificationCompat.Builder  builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.kakebelogo)
                .setContentTitle("Order Placed 🎉🎊🙌 💯")
                .setContentText(user.getFullname()+ getString(R.string.placed_order_message))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(user.getFullname()+ getString(R.string.placed_order_message)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notification = builder.build();
        notificationManagerCompat = NotificationManagerCompat.from(this);


    }




}