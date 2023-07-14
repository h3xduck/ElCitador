package com.example.elcitador;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Spinner;

public class PosterScheduler {
    public static void scheduleTask(Context context, Integer especialidad, String motivo) {
        //Delete previous alarms
        deletePreviousAlarms(context);

        //Schedule the new alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, PosterBroadcastReceiver.class);
        intent.putExtra("especialidad", especialidad); // Pass the integer value
        intent.putExtra("motivo", motivo); // Pass the string value
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        // Set the interval for the periodic task: 1 minute
        long intervalMillis = 120000L;
        //long intervalMillis = 3000L;
        // Schedule the task
        //alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+intervalMillis, pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + intervalMillis, intervalMillis, pendingIntent);
    }

    private static void deletePreviousAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, PosterBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        Log.d(TAG, "Deleted previous alarms");
    }

}
