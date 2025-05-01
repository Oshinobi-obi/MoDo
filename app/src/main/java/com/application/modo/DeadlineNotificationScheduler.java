package com.application.modo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class DeadlineNotificationScheduler {

    public static void schedule(Context context, String taskTitle, String deadlineTime, long triggerAtMillis) {
        String message = "Your task \"" + taskTitle + "\" is due on " + deadlineTime;
        scheduleWithMessage(context, taskTitle, message, triggerAtMillis);
    }

    public static void scheduleWithMessage(Context context, String taskTitle, String message, long triggerAtMillis) {
        if (triggerAtMillis < System.currentTimeMillis()) return;

        Intent intent = new Intent(context, DeadlineNotificationReceiver.class);
        intent.putExtra("taskTitle", taskTitle);
        intent.putExtra("customMessage", message);

        int requestCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }
}