package com.example.taskandconsequence;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.util.TypedValue;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.example.taskandconsequence.db.DatabaseHelper;
import com.example.taskandconsequence.model.ProgramOccurrence;
import com.example.taskandconsequence.model.Task;

import java.util.List;

public class NotificationsAlarmReceiver extends BroadcastReceiver {

    private DatabaseHelper databaseHelper;
    private Context context;
    private NotificationManagerCompat notificationManager;
    private final int GROUP_TASKS = 0;
    private final int GROUP_PUNISHMENT = 1;


    @Override
    public void onReceive(Context context, Intent intent) {
        // Fetch content from the database for notification
        try{
            databaseHelper = new DatabaseHelper(context);
            this.context = context;
            notificationManager = NotificationManagerCompat.from(context);
            // Create a notification channel for API 26+

            List<ProgramOccurrence> pending = databaseHelper.getPendingProgramOccurrencesToday();
            for (ProgramOccurrence programOccurrence : pending)
                displayNotification(programOccurrence, false);
            if (pending.size() > 0)
                displayNotificationSummary(false, pending);

            List<ProgramOccurrence> pendingPunishments = databaseHelper.getPendingPunishmentProgramOccurrencesToday();
            for (ProgramOccurrence programOccurrence : pendingPunishments)
                displayNotification(programOccurrence, true);

            if (pendingPunishments.size() > 0)
                displayNotificationSummary(true, pendingPunishments);

            databaseHelper.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void displayNotificationSummary(boolean isPunishment, List<ProgramOccurrence> occurrences) {
        String title, description, summary;
        if (!isPunishment) {
            title = "Today's Schedules";
            description = String.format("%d Schedules are planed for today", occurrences.size());
            summary = String.format("%d Schedules Today", occurrences.size());
        } else {
            title = "Today's Punishments";
            description = String.format("%d Punishments should be taken today", occurrences.size());
            summary = String.format("%d Punishments today", occurrences.size());
        }
        Notification summaryNotification =
                new NotificationCompat.Builder(context, "task_0101")
                        .setContentTitle(title)
                        // Set content text to support devices running API level < 24.
                        .setContentText(description)
                        .setSmallIcon(R.drawable.ic_notifications_small_icon)
                        // Build summary info into InboxStyle template.
                        .setStyle(new NotificationCompat.InboxStyle()
//                                .addLine("Alex Faarborg  Check this out")
//                                .addLine("Jeff Chang    Launch Party")
//                                .setBigContentTitle("2 new messages")
                                .setSummaryText(summary))
                        // Specify which group this notification belongs to.
                        .setGroup(isPunishment ? "task_punishment" : "task_todo")
                        // Set this notification as the summary for the group.
                        .setGroupSummary(true)
                        .build();

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(isPunishment ? GROUP_PUNISHMENT : GROUP_TASKS, summaryNotification);
    }

    public void displayNotification(ProgramOccurrence occurrence, boolean isPunishment) {
        // Build and display the notification
        String title;
        String description = "";
        String shortDesc = "";
        if (isPunishment) {
            title = occurrence.getProgram().getSmallPunishment().getName();
            shortDesc = "Punishment for not fulfilling " + occurrence.getProgram().getName();
            description = occurrence.getProgram().getSmallPunishment().getDescription();
        } else {
            title = occurrence.getProgram().getName();
            shortDesc = "Reminder.";
            description = "you should do the following tasks during this day\n";
            int counter = 1;
            for (Task task : occurrence.getProgram().getTasks()) {
                description = description + counter + "- " + task.getName() + "\n";
                counter++;
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task_0101")
                .setContentTitle(title)   // set the title of the notification
                .setContentText(shortDesc) // set the text for the notification
                .setSmallIcon(R.drawable.ic_notifications_small_icon)
                .setLargeIcon(getBitmapFromVectorDrawable(isPunishment))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(description))
                .setGroup(isPunishment ? "task_punishment" : "task_todo")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                ;


// Set the large icon

        // Optional: Add actions and other notification settings
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("occurrenceId", occurrence.getId().toString()); // Custom extra to identify the target fragment

        PendingIntent pendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, occurrence.getId().intValue(), intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, occurrence.getId().intValue(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        builder.setContentIntent(pendingIntent);


        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(occurrence.getId().intValue(), builder.build());

    }

    private Bitmap getBitmapFromVectorDrawable(boolean isPunishment) {
        int drawableId = isPunishment ? R.drawable.ic_punishments : R.drawable.baseline_task_alt_24;
//        int tintColor = isPunishment ? getThemeColor(com.google.android.material.R.attr.colorError) : getThemeColor(com.google.android.material.R.attr.colorSecondary);
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof VectorDrawableCompat || drawable instanceof VectorDrawable) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());

            // Apply tint color
//            drawable.mutate().setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));

            drawable.draw(canvas);
            return bitmap;
        } else {
            throw new IllegalArgumentException("Unsupported drawable type");
        }


    }

    private int getThemeColor(int attributeColor) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attributeColor, typedValue, true);
        return typedValue.data;
    }

}

