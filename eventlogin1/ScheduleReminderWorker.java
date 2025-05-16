package com.example.eventlogin1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ScheduleReminderWorker extends Worker {
    private static final String NOTIFICATION_CHANNEL_ID = "EVENT_REMINDER_CHANNEL";
    private static final String TAG = "ReminderWorker";

    public ScheduleReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Notification permission not granted in Worker");
                return Result.failure();
            }
        }

        String eventTitle = getInputData().getString("EVENT_NAME");
        if (eventTitle == null) {
            eventTitle = "Event Reminder";
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Event Reminder")
                .setContentText("Your event \"" + eventTitle + "\"is going to  start in 1 minute!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

        Log.d(TAG, "Reminder notification sent for event: " + eventTitle);
        return Result.success();
    }
}
