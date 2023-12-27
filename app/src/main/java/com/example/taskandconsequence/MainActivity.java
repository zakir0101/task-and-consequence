package com.example.taskandconsequence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.taskandconsequence.databinding.ActivityMainBinding;
import com.example.taskandconsequence.db.DatabaseHelper;
import com.example.taskandconsequence.model.Backup;
import com.example.taskandconsequence.viewmodel.SharedViewModel;
import com.example.taskandconsequence.viewmodel.SharedViewModelFactory;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;

import com.google.api.services.drive.model.File;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String GOOGLE_NAME = "google_name";
    private ActivityMainBinding binding;
    private SharedViewModel viewModel;

    private NavController navController;

    private final Integer CREATE_FILE_REQUEST_CODE = 1;
    private final Integer READ_REQUEST_CODE = 2;
    private final Integer NOTIFICATION_REQUEST_CODE = 3;
    private final Integer RC_SIGN_IN = 4;
    public final Integer REQUEST_AUTHORIZATION = 5;
    public final Integer BACKUP_ALARM_REQUEST_CODE = 6 ;
    public static final Integer BACKUP_ALARM_RETRY_REQUEST_CODE = 7 ;
    public static final Integer BACKUP_NOTIFICATION_REQUEST_CODE = 8 ;
    public final Integer NOTIFICATIONS_ALARM_REQUEST_CODE = 9 ;
    public static final String PREFS_NAME = "MyPrefsFile";

    private static final String ALARM_SET_KEY = "alarmScheduled";
    public static final String GOOGLE_ID_TOKEN = "google_key";

    public static final String FILE_ID = "file_id";
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SharedViewModelFactory factory = new SharedViewModelFactory(databaseHelper);
        viewModel = new ViewModelProvider(this, factory).get(SharedViewModel.class);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
//                .requestIdToken("52812651195-2nt0q3tah305evq9podg7tphsu1fcqfe.apps.googleusercontent.com")
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))

                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
//        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController);
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.programsFragment:
                    // Navigate to ProgramsFragment
                    viewModel.startDestinationSetup();
                    invalidateMenu();
                    navController.navigate(R.id.programsFragment);
                    break;
                case R.id.activeProgramOccurrenceFragment:
                    // Navigate to ActiveProgramOccurrenceFragment
                    viewModel.startDestinationSetup();
                    invalidateMenu();

                    navController.navigate(R.id.activeProgramOccurrenceFragment);
                    break;
                case R.id.tasksFragment:
                    // Navigate to TasksFragment
                    viewModel.startDestinationSetup();
                    invalidateMenu();

                    navController.navigate(R.id.tasksFragment);
                    break;
                case R.id.punishmentsFragment:

                    // Navigate to PunishmentsFragment
                    viewModel.startDestinationSetup();
                    invalidateMenu();
                    navController.navigate(R.id.punishmentsFragment);
                    break;
            }
            return true;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        String occurrenceId = intent.getStringExtra("occurrenceId");

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("application/json".equals(type)) {
                Uri fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (fileUri != null) {
                    // Handle the received JSON file
                    readFromFile(fileUri);
                }
            }
        } else if (occurrenceId != null) {
            viewModel.activeOccurrenceId = Long.valueOf(occurrenceId);
            navController.navigate(R.id.taskOccurrenceFragment);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_REQUEST_CODE);
            } else {
                setupAlarmReceiver();
            }
        } else
            setupAlarmReceiver();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                setupAlarmReceiver();
            } else {
                // Permission denied
            }
        }
    }

    private void setupAlarmReceiver() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean alarmScheduled = settings.getBoolean(ALARM_SET_KEY, false);

        if (!alarmScheduled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Task and Consequences";
                String description = "and app for managing task , and motivating user to do them";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel1 = new NotificationChannel("task_0101", name, importance);
                channel1.setDescription(description);
//                NotificationChannel channel2 = new NotificationChannel("task_punishment", name, importance);
//                channel2.setDescription(description);
                NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel1);
//                notificationManager.createNotificartionChannel(channel2);
            }

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intentAlarm = new Intent(this, NotificationsAlarmReceiver.class);
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getBroadcast(this, NOTIFICATIONS_ALARM_REQUEST_CODE, intentAlarm, PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = PendingIntent.getBroadcast(this, NOTIFICATIONS_ALARM_REQUEST_CODE, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
            }
//            alarmManager.cancel(pendingIntent);
            // Set the alarm to start at 6:00 AM
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 6);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // Check if the alarm time is in the past and adjust the date accordingly
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
//            long thirtySecondsInMillis = 20 * 1000; // 2 minutes in milliseconds
//            long tenMinutesInMillis = 10 * 60 * 1000; // 2 minutes in milliseconds

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP
                    , calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_HOUR * 6, pendingIntent);

            // Save the alarmScheduled flag
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(ALARM_SET_KEY, true);
            editor.apply();
        }
    }

    public View getBottomNavigationView() {
        return binding.bottomNavigationView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        addStaticMenuItems(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);


        if (viewModel.isSelectionModeActive) {
            removeStaticMenuItems(menu);
            addSelectionMenuItem(menu);
            addDeleteMenuItem(menu);

            if (navController.getCurrentDestination().getId() == R.id.programsFragment
                    && viewModel.selectedItems.size() == 1) {
                addEditMenuItem(menu);
            } else {
                removeEditMenuItems(menu);
            }
        } else {
            removeDeleteMenuItems(menu);
            removeSelectionMenuItems(menu);
            addStaticMenuItems(menu);
        }

        return true;
    }

    private void addStaticMenuItems(Menu menu) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String apiKey = settings.getString(GOOGLE_ID_TOKEN, null);
        String displayName = settings.getString(GOOGLE_NAME, "");
        if (apiKey != null) {
            if (menu.findItem(R.id.menu_item_name) == null)
                menu.add(0, R.id.menu_item_name, 0, displayName)
                        .setEnabled(false)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        } else {
            menu.removeItem(R.id.menu_item_name);

        }

        if (menu.findItem(R.id.menu_item_save) == null)

            menu.add(0, R.id.menu_item_save, 40, "Save All")
                    .setIcon(R.drawable.baseline_save_alt_24).
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        if (menu.findItem(R.id.menu_item_load) == null)
            menu.add(0, R.id.menu_item_load, 50, "load file")
                    .setIcon(R.drawable.baseline_load_24)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        if (menu.findItem(R.id.menu_item_share_all) == null)
            menu.add(0, R.id.menu_item_share_all, 60, "Share All")
                    .setIcon(R.drawable.baseline_share_24)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);


        if (apiKey == null) {

            menu.removeItem(R.id.menu_item_save_drive);
            menu.removeItem(R.id.menu_item_save_drive);
            menu.removeItem(R.id.menu_item_logout);
            if (menu.findItem(R.id.menu_item_sign_in) == null)
                menu.add(0, R.id.menu_item_sign_in, 70, "Sign in")
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        } else {
            menu.removeItem(R.id.menu_item_sign_in);
            if (menu.findItem(R.id.menu_item_save_drive) == null)
                menu.add(0, R.id.menu_item_save_drive, 80, "Save to Drive")
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            if (menu.findItem(R.id.menu_item_load_drive) == null)
                menu.add(0, R.id.menu_item_load_drive, 90, "Load from Drive")
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            if (menu.findItem(R.id.menu_item_logout) == null)
                menu.add(0, R.id.menu_item_logout, 100, "logout")
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        }
    }

    private void removeStaticMenuItems(@NonNull Menu menu) {
        menu.removeItem(R.id.menu_item_load);
        menu.removeItem(R.id.menu_item_save);
        menu.removeItem(R.id.menu_item_share_all);
        menu.removeItem(R.id.menu_item_sign_in);
        menu.removeItem(R.id.menu_item_save_drive);
        menu.removeItem(R.id.menu_item_load_drive);
        menu.removeItem(R.id.menu_item_name);
        menu.removeItem(R.id.menu_item_logout);
    }

    private void removeSelectionMenuItems(Menu menu) {
        menu.removeItem(R.id.menu_item_export_selected);
        menu.removeItem(R.id.menu_item_select_all);
        menu.removeItem(R.id.menu_item_share_selected);
    }

    private void addSelectionMenuItem(@NonNull Menu menu) {
        if (menu.findItem(R.id.menu_item_export_selected) == null)
            menu.add(0, R.id.menu_item_export_selected, 0, "Export Selected")
                    .setIcon(R.drawable.baseline_save_alt_24).
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        if (menu.findItem(R.id.menu_item_select_all) == null)
            menu.add(0, R.id.menu_item_select_all, 0, "Select All")
                    .setIcon(R.drawable.baseline_select_all_24).
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        if (menu.findItem(R.id.menu_item_share_selected) == null)
            menu.add(0, R.id.menu_item_share_selected, 0, "Share")
                    .setIcon(R.drawable.baseline_share_24).
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    private void removeEditMenuItems(@NonNull Menu menu) {
        menu.removeItem(R.id.menu_item_edit);
    }

    private void removeDeleteMenuItems(@NonNull Menu menu) {
        menu.removeItem(R.id.menu_item_delete);
    }

    private void addEditMenuItem(@NonNull Menu menu) {
        if (menu.findItem(R.id.menu_item_edit) == null)
            menu.add(0, R.id.menu_item_edit, 0, "Edit")
                    .setIcon(R.drawable.baseline_edit_24).
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

    }

    private void addDeleteMenuItem(@NonNull Menu menu) {

        if (menu.findItem(R.id.menu_item_delete) == null)
            menu.add(1, R.id.menu_item_delete, 1, "Delete")
                    .setIcon(R.drawable.baseline_delete_24)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle back button action
        if (item.getItemId() == android.R.id.home) {
            navController.navigateUp();
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_item_save:
                onExportAll();
                return true;
            case R.id.menu_item_load:
                onLoadBackup();
                return true;
            case R.id.menu_item_share_all:
                onShareAll();
                return true;
            case R.id.menu_item_sign_in:
                onSignIn();
                return true;
            case R.id.menu_item_save_drive:
                saveToGoogleDrive();
                return true;
            case R.id.menu_item_load_drive:
                loadFromGoogleDrive();
                return true;
            case R.id.menu_item_logout:
                logout();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mGoogleSignInClient.signOut();
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(GOOGLE_ID_TOKEN, null);
        editor.putString(GOOGLE_NAME, null);
        editor.putString(FILE_ID, null);
        editor.apply();
        Intent intent = new Intent(this, BackupAlarmReceiver.class);
        AlarmManager alarmManager =
                (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(this,BACKUP_ALARM_REQUEST_CODE , intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(this, BACKUP_ALARM_REQUEST_CODE, intent,PendingIntent.FLAG_NO_CREATE |  PendingIntent.FLAG_UPDATE_CURRENT );
        }
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        invalidateMenu();
    }

    private void loadFromGoogleDrive() {
        MutableLiveData<Backup> backupMutableLiveData = new MutableLiveData<>();
        backupMutableLiveData.observe(this , backup -> {
            if (backup == null)
                Toast.makeText(this, "Could not Load Backup from Drive.",Toast.LENGTH_SHORT).show();
            else{
                showBackupDialog(backup);
            }
        });

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        Drive googleDriveService = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE))
                        .setSelectedAccount(account.getAccount()))
                .setApplicationName(getString(R.string.app_name))
                .build();

        Executors.newSingleThreadExecutor().execute(() -> {
            FileList result = null;
            try {
                result = googleDriveService.files().list()
                        .setQ("name = 'task_and_consequence_backup.json' and mimeType = 'application/json'")
                        .setSpaces("drive")
                        .setFields("files(id, name, createdTime)")
                        .setOrderBy("createdTime desc") // Order by creation time in descending order
                        .execute();

                List<File> files = result.getFiles();
                if (files != null && !files.isEmpty()) {
                    // The first file in the list is the most recent
                    File latestBackupFile = files.get(0);
                    // Use latestBackupFile.getId() to download and restore data
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    googleDriveService.files().get(latestBackupFile.getId()).executeMediaAndDownloadTo(outputStream);
                    String jsonContent = outputStream.toString();
                    Backup backup = Backup.fromJson(new JSONObject(jsonContent));
                    backupMutableLiveData.postValue(backup);
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(FILE_ID, latestBackupFile.getId());
                    editor.apply();
                }
            }catch (UserRecoverableAuthIOException e) {
                backupMutableLiveData.postValue(null);
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            }
            catch (JSONException | IOException e) {
                backupMutableLiveData.postValue(null);
//                throw new RuntimeException(e);
            }

        });
    }

    private void saveToGoogleDrive() {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        Drive googleDriveService = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE))
                        .setSelectedAccount(account.getAccount()))
                .setApplicationName(getString(R.string.app_name))
                .build();
        viewModel.getAllBackup().observe(this, backup -> {
            String fileName = "task_and_consequence_backup.json";
            File fileMetadata = new File();

            fileMetadata.setName(fileName);
            fileMetadata.setMimeType("application/json");
            java.io.File filePath = getFileFromBackup(backup, fileName);
            FileContent mediaContent = new FileContent("application/json", filePath);
            MutableLiveData<Boolean> done = new MutableLiveData<>();
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

            String oldFileId = settings.getString(FILE_ID, null);

            done.observe(this, isDone -> {
                String msg = "";
                if (isDone & oldFileId != null)
                    msg = "Backup updated successfully";
                else if (isDone)
                    msg = "Backup created Successfully";
                else
                    msg = "Could not Save Backups. check Network Connectivity";
                Toast.makeText(this , msg , Toast.LENGTH_SHORT).show();

            });
            Executors.newSingleThreadExecutor().execute(() -> {

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
                    done.postValue(true);
                }catch (UserRecoverableAuthIOException e) {
                    done.postValue(false);
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                }

                catch (IOException e) {
                    done.postValue(false);
//                    throw new RuntimeException(e);
                }

            });

        });


    }

    private void onSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    public void onExportSelected(Backup backup, boolean share) {
        int hasPunishment = backup.getPunishments().size() > 0 ? 1 : 0;
        int hasMusterTasks = backup.getTasks().size() > 0 ? 1 : 0;
        int hasPrograms = backup.getPrograms().size() > 0 ? 1 : 0;
        String fileName = "";
        if (hasPrograms + hasPunishment + hasMusterTasks > 1)
            fileName = "backup.json";
        else if (hasPrograms == 1)
            fileName = "programs.json";
        else if (hasPunishment == 1)
            fileName = "punishments.json";
        else if (hasMusterTasks == 1)
            fileName = "tasks.json";
        viewModel.backup = backup;
        if (share) {
            Uri fileUri;
            // Convert object to JSON
            java.io.File tempFile = getFileFromBackup(backup, fileName);
            fileUri = FileProvider.getUriForFile(this, "com.example.taskandconsequence.fileprovider", tempFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/json");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            startActivity(Intent.createChooser(shareIntent, "Share Backup"));

        } else {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, fileName); // Suggests a default filename
            startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);

        }
    }

    private java.io.File getFileFromBackup(Backup backup, String fileName) {
        Uri fileUri;
        String jsonString = backup.toJson().toString();

        // Create a temporary file
        java.io.File cachePath = new java.io.File(getCacheDir(), "temp");
        cachePath.mkdirs();
        java.io.File tempFile = new java.io.File(cachePath, fileName);
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(tempFile);

            stream.write(jsonString.getBytes());
            stream.close();
            return tempFile;


        } catch (IOException e) {
            fileUri = null;
//            return null;
            throw new RuntimeException(e);
        }

    }

    private void onLoadBackup() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, READ_REQUEST_CODE); // Use a unique request code
    }


    private void onExportAll() {
        viewModel.getAllBackup().observe(this, backup -> {
            onExportSelected(backup, false);
        });
    }

    private void onShareAll() {
        viewModel.getAllBackup().observe(this, backup -> {
            onExportSelected(backup, true);
        });
    }


    private void showBackupDialog(Backup backup) {
        viewModel.backup = backup;
        boolean hasPunishment = backup.getPunishments().size() > 0;
        boolean hasMusterTasks = backup.getTasks().size() > 0;
        boolean hasPrograms = backup.getPrograms().size() > 0;
        String text = "the following data will be loaded :\n" +
                (hasPrograms ? String.format("%d Programs.\n", backup.getPrograms().size()) : "") +
                (hasMusterTasks ? String.format("%d Tasks.\n", backup.getTasks().size()) : "") +
                (hasPunishment ? String.format("%d Punishment.\n", backup.getPunishments().size()) : "") +
                "do you add them to the existing data or" +
                " replace the existing data with them ?";

        new AlertDialog.Builder(this)
                .setTitle("Load Backup")
                .setMessage(text)
                .setNeutralButton("Replace", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBackupReplace(dialog);

                    }
                })

                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBackupAddToExisting(dialog);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();


    }

    private void onBackupAddToExisting(DialogInterface dialog) {
        Backup backup = viewModel.backup;
        viewModel.loadBackup(backup).observe(this, done -> {
            if (done)
                Toast.makeText(this, "Backup loaded successfully", Toast.LENGTH_SHORT).show();
        });
        dialog.dismiss();
        int hasPunishment = backup.getPunishments().size() > 0 ? 1 : 0;
        int hasMusterTasks = backup.getTasks().size() > 0 ? 1 : 0;
        int hasPrograms = backup.getPrograms().size() > 0 ? 1 : 0;
        boolean navigate = (hasPrograms + hasPunishment + hasMusterTasks) == 1;
        if (hasPrograms == 1) {
            if (navigate)
                navController.navigate(R.id.programsFragment);
        }
        if (hasPunishment == 1) {
            if (navigate)
                navController.navigate(R.id.punishmentsFragment);

        }
        if (hasMusterTasks == 1) {
            if (navigate)
                navController.navigate(R.id.tasksFragment);

        }
    }

    private void onBackupReplace(DialogInterface dialog) {
        Backup backup = viewModel.backup;

        int hasPunishment = backup.getPunishments().size() > 0 ? 1 : 0;
        int hasMusterTasks = backup.getTasks().size() > 0 ? 1 : 0;
        int hasPrograms = backup.getPrograms().size() > 0 ? 1 : 0;
        if (hasPrograms == 1)
            viewModel.deleteAllPrograms();
        if (hasPunishment == 1)
            viewModel.deleteAllPunishments();
        if (hasMusterTasks == 1)
            viewModel.deleteAllTasks();

        onBackupAddToExisting(dialog);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                writeToFile(uri);
            }
        } else if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                readFromFile(uri);
            }
        } else if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }else if ( requestCode ==  REQUEST_AUTHORIZATION  ){
            if (resultCode == RESULT_OK) {
                // User has granted permission; retry the Google Drive operation
                // Example: saveToGoogleDrive(); or the relevant method that needs Drive access
                Toast.makeText(this,"now you can backup data with Google Drive",Toast.LENGTH_SHORT).show();
//                saveToGoogleDrive();
            }
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {

            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI and save to SharedPreferences
            saveToSharedPreferences(account.getId(), account.getDisplayName());
            Log.e("DisplayName", account.getDisplayName());

            this.invalidateMenu();
            this.loadFromGoogleDrive();
            this.setupBackupAlarm();
        } catch (ApiException e) {
            Log.e("SignInError", "signInResult: failed code=" + e.getStatusCode());
            Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
        }

    }

    private void saveToSharedPreferences(String key, String displayName) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(GOOGLE_ID_TOKEN, key);
        editor.putString(GOOGLE_NAME, displayName);
        editor.apply();

    }

    private void setupBackupAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task and Consequences";
            String description = "and app for managing task , and motivating user to do them";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel1 = new NotificationChannel("task_backup_0101", name, importance);
            channel1.setDescription(description);
            NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel1);
        }


        Log.e("BackupAlarm","setting backup alarms");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, BackupAlarmReceiver.class);
        PendingIntent pendingIntent ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(this, BACKUP_ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(this, BACKUP_ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 3);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

//        Long thirtyMinutes = 1000 * 60 * 30L;
//        long thirtySecond = 1000 * 20L;
//        alarmManager.cancel(pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis() ,
                AlarmManager.INTERVAL_DAY, pendingIntent);

    }


    private void readFromFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStream.close();
            String jsonContent = stringBuilder.toString();
            Backup backup = Backup.fromJson(new JSONObject(jsonContent));
            viewModel.backup = backup;
            showBackupDialog(backup);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            Toast.makeText(this, "File format is not Valid", Toast.LENGTH_SHORT).show();
        }
    }


    private void writeToFile(Uri uri) {
        try {
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
            String jsonContent = viewModel.backup.toJson().toString();
            fileOutputStream.write(jsonContent.getBytes());
            fileOutputStream.close();
            pfd.close();
            Toast.makeText(this, uri.getLastPathSegment() + " saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
