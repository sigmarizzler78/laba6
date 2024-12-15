package com.example.lb2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.lb2.R;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Получаем данные из намерения
        String title = intent.getStringExtra("TITLE");
        String message = intent.getStringExtra("MESSAGE");

        // Создание намерения для открытия MainActivity при нажатии на уведомление
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra("MESSAGE", message);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Создание канала уведомлений для Android O и выше
        String channelId = "reminder_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Напоминания", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Канал для уведомлений о напоминаниях");
            notificationManager.createNotificationChannel(channel);
        }

        // Создание уведомления
        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.car) // Убедитесь, что у вас есть этот ресурс
                .setContentIntent(pendingIntent)
                .setAutoCancel(true) // Уведомление исчезнет при нажатии
                .build();

        // Уникальный идентификатор уведомления
        int notificationId = (int) System.currentTimeMillis(); // Используем текущее время для уникальности
        notificationManager.notify(notificationId, notification);
    }
}