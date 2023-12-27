package com.example.taskandconsequence;

import static com.example.taskandconsequence.MainActivity.BACKUP_NOTIFICATION_REQUEST_CODE;
import static com.example.taskandconsequence.MainActivity.FILE_ID;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.MutableLiveData;

import com.example.taskandconsequence.db.DatabaseHelper;
import com.example.taskandconsequence.model.Backup;
import com.example.taskandconsequence.model.ProgramOccurrence;
import com.example.taskandconsequence.model.Task;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Executors;

public class BackupAlarmReceiver extends BroadcastReceiver {
    private DatabaseHelper databaseHelper;
    private Context context;
    private NotificationManagerCompat notificationManager;

    private SharedPreferences settings;
    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            this.context = context;
            databaseHelper = new DatabaseHelper(context);
            notificationManager = NotificationManagerCompat.from(context);

            settings = context.getSharedPreferences(MainActivity.PREFS_NAME, 0);
            String googleIdToken = settings.getString(MainActivity.GOOGLE_ID_TOKEN, null);
            if (isNetworkAvailable(context)) {
                if (googleIdToken != null) {
                    saveToGoogleDrive();
                }
            } else {
                // Schedule the alarm for 2 hours later if network is not available
                scheduleOneTimeAlarm(context, 2 * 60 * 60 * 1000); // 2 hours in milliseconds
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void scheduleOneTimeAlarm(Context context, long delayMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent retryIntent = new Intent(context, BackupAlarmReceiver.class);
        // Specify the mutability flag based on the Android version
        int flag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ? PendingIntent.FLAG_IMMUTABLE : 0;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MainActivity.BACKUP_ALARM_RETRY_REQUEST_CODE, retryIntent, flag);

        long triggerTime = System.currentTimeMillis() + delayMillis;
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void saveToGoogleDrive() {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        Drive googleDriveService = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_FILE))
                        .setSelectedAccount(account.getAccount()))
                .setApplicationName(context.getString(R.string.app_name))
                .build();
        Backup backup = databaseHelper.getAllBackup();
        String fileName = "task_and_consequence_backup.json";
        File fileMetadata = new File();

        fileMetadata.setName(fileName);
        fileMetadata.setMimeType("application/json");
        java.io.File filePath = getFileFromBackup(backup, fileName);
        FileContent mediaContent = new FileContent("application/json", filePath);
        MutableLiveData<Boolean> done = new MutableLiveData<>();

        String oldFileId = settings.getString( FILE_ID, null);

//        done.observe(context, isDone -> {
//            displayNotification(isDone,oldFileId != null);
//        });
        Executors.newSingleThreadExecutor().execute (() -> {
            File file = null;
            try {
                if (oldFileId != null)
                    googleDriveService.files().delete(oldFileId).execute();
                file = googleDriveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(FILE_ID, file.getId());
                editor.apply();
                Log.e("GOOGLE DRIVE", "File ID: " + file.getId());
                displayNotification(true, oldFileId != null);
            } catch (IOException e) {
                displayNotification(false, oldFileId != null);
                scheduleOneTimeAlarm(context, 2 * 60 * 60 * 1000);
            }
        });

    }

    private java.io.File getFileFromBackup(Backup backup, String fileName) {
        Uri fileUri;
        String jsonString = backup.toJson().toString();

        // Create a temporary file
        java.io.File cachePath = new java.io.File(context.getCacheDir(), "temp");
        cachePath.mkdirs();
        java.io.File tempFile = new java.io.File(cachePath, fileName);
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(tempFile);

            stream.write(jsonString.getBytes());
            stream.close();
            return tempFile;


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public void displayNotification( boolean isSuccessful , boolean isFileId) {
        // Build and display the notification
        String title = "Backup";
        String description = "";
        String shortDesc = "";
        if (isSuccessful &  isFileId) {
            shortDesc = "Successful";
            description = "Backup updated successfully";
        } else if(isSuccessful){
            shortDesc = "Successful";
            description = "Backup created successfully";

        }
        else {
            shortDesc = "Failed.";
            description = "Could not Save Backups. check Network Connectivity. Rescheduling the Alarm in 1 Hours.";

        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task_backup_0101")
                .setContentTitle(title)   // set the title of the notification
                .setContentText(shortDesc) // set the text for the notification
                .setSmallIcon(R.drawable.ic_notifications_small_icon)
//                .setLargeIcon(getBitmapFromVectorDrawable(isPunishment))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                ;



        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        intent.putExtra("occurrenceId", occurrence.getId().toString()); // Custom extra to identify the target fragment

        PendingIntent pendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, MainActivity.BACKUP_NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, BACKUP_NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        builder.setContentIntent(pendingIntent);


        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(3, builder.build());

    }

}
