package com.mycompany.passman;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;
import java.util.Map;

public class Notifications extends BroadcastReceiver {
    public static void setAlarm(Context context, String ID){
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE),
                notifications = context.getApplicationContext().getSharedPreferences("notifications", Context.MODE_PRIVATE);

        notifications.edit().putBoolean(ID, true).apply();

        Calendar calendar = Calendar.getInstance();
        // for debug change DAY_OF_YEAR to SECONDS
        calendar.add(Calendar.DAY_OF_YEAR, settings.getInt("days", 30));
        long alertTime = calendar.getTimeInMillis();

        Intent alertIntent = new Intent(context, Notifications.class).putExtra("ID", ID);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(context, ID.hashCode(), alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void remindAlarm(Context context, String ID) {
        Calendar calendar = Calendar.getInstance();
        // for debug change DAY_OF_YEAR to SECONDS
        calendar.add(Calendar.DAY_OF_YEAR, 10);
        long alertTime = calendar.getTimeInMillis();

        Intent alertIntent = new Intent(context, Notifications.class).putExtra("ID", ID);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(context, ID.hashCode(), alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void cancelAlarm(Context context, String ID) {
        SharedPreferences notifications = context.getApplicationContext().getSharedPreferences("notifications", Context.MODE_PRIVATE);
        notifications.edit().putBoolean(ID, false).apply();
        Intent intent = new Intent(context, Notifications.class).putExtra("ID", ID);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(context, ID.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void cancelAll(Context context){
        SharedPreferences notifications = context.getApplicationContext().getSharedPreferences("notifications", Context.MODE_PRIVATE);
        Map<String, ?> keys = notifications.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()) {
            notifications.edit().putBoolean(entry.getKey(), false).apply();
            cancelAlarm(context, entry.getKey());
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String temp = intent.getStringExtra("ID");
        createNotification(context, intent.getStringExtra("ID").hashCode(), "Reminder to update password", temp);
        remindAlarm(context, intent.getStringExtra("ID"));
    }

    public void createNotification(Context context, int ID, String msg, String msgText) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_action_edit)
                .setContentTitle(msg)
                .setContentText(msgText);

        Intent newIntent = new Intent(context, Accounts.class);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(Accounts.class);
        taskStackBuilder.addNextIntent(newIntent);

        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(ID, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);
        builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        builder.setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID, builder.build());
    }
}
