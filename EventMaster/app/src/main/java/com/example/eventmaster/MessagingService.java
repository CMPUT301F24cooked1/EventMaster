package com.example.eventmaster;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Receives notifications from firebase messaging
 */
public class MessagingService extends FirebaseMessagingService {
    //www.youtube.com/watch?v=spbSMpjONGc, 2024-11-24

    private NotificationManager notificationManager;

    /**
     * Denotes what to do upon receiving a new device token
     * @param token Device's token used for notifications
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        //updateNewToken(token);
    }

    /**
     * Denotes what to do upon receiving a message from firebase
     * @param message Message data sent from firebase
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Log.d("Notification", "Message Recieved from firebase.");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Notification");

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentTitle(Objects.requireNonNull(message.getNotification().getTitle()));
        builder.setContentText(message.getNotification().getBody());
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message.getNotification().getBody()));
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.app_logo);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setContentIntent(pendingIntent);

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "Notification";
        NotificationChannel channel = new NotificationChannel(
                channelId, "Coding", NotificationManager.IMPORTANCE_HIGH
        );
        channel.enableLights(true);
        channel.canBypassDnd();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            channel.canBubble();
        }

        notificationManager.createNotificationChannel(channel);
        builder.setChannelId(channelId);
        notificationManager.notify(1, builder.build());
    }
}
