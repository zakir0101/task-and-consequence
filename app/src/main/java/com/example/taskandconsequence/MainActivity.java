package com.example.taskandconsequence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.taskandconsequence.databinding.ActivityMainBinding;
import com.example.taskandconsequence.db.DatabaseHelper;
import com.example.taskandconsequence.model.Backup;
import com.example.taskandconsequence.viewmodel.SharedViewModel;
import com.example.taskandconsequence.viewmodel.SharedViewModelFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedViewModel viewModel;

    private NavController navController;

    private final Integer CREATE_FILE_REQUEST_CODE = 1;
    private final Integer READ_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SharedViewModelFactory factory = new SharedViewModelFactory(databaseHelper);
        viewModel = new ViewModelProvider(this, factory).get(SharedViewModel.class);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("application/json".equals(type)) {
                Uri fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (fileUri != null) {
                    // Handle the received JSON file
                    readFromFile(fileUri);
                }
            }
        }

    }

    public View getBottomNavigationView() {
        return binding.bottomNavigationView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        addSaveLoadMenuItems(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);


        if (viewModel.isSelectionModeActive) {
            removeSaveLoadMenuItems(menu);
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
            addSaveLoadMenuItems(menu);
        }

        return true;
    }

    private void addSaveLoadMenuItems(Menu menu) {
        if (menu.findItem(R.id.menu_item_save) == null)

            menu.add(0, R.id.menu_item_save, 0, "Save All")
                    .setIcon(R.drawable.baseline_save_alt_24).
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        if (menu.findItem(R.id.menu_item_load) == null)
            menu.add(0, R.id.menu_item_load, 0, "load file")
                    .setIcon(R.drawable.baseline_load_24)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        if (menu.findItem(R.id.menu_item_share_all) == null)
            menu.add(0, R.id.menu_item_share_all, 0, "Share All")
                    .setIcon(R.drawable.baseline_share_24)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

    }

    private void removeSaveLoadMenuItems(@NonNull Menu menu) {
        menu.removeItem(R.id.menu_item_load);
        menu.removeItem(R.id.menu_item_save);
        menu.removeItem(R.id.menu_item_share_all);
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



        }
        return super.onOptionsItemSelected(item);
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
            // Convert object to JSON
            String jsonString = backup.toJson().toString();

            // Create a temporary file
            File cachePath = new File(getCacheDir(), "temp");
            cachePath.mkdirs();
            File tempFile = new File(cachePath, fileName);
            FileOutputStream stream = null;
            try {
                stream = new FileOutputStream(tempFile);

                stream.write(jsonString.getBytes());
                stream.close();
                Uri fileUri = FileProvider.getUriForFile(this, "com.example.taskandconsequence.fileprovider", tempFile);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/json");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                startActivity(Intent.createChooser(shareIntent, "Share Backup"));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, fileName); // Suggests a default filename
            startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);

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
            onExportSelected(backup,false);
        });
    }
    private void onShareAll() {
        viewModel.getAllBackup().observe(this, backup -> {
            onExportSelected(backup,true);
        });
    }


    private void showBackupDialog(Backup backup) {

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
        boolean navigate = (hasPrograms + hasPunishment + hasMusterTasks) == 1 ;
        if (hasPrograms == 1 ) {
            if ( navigate)
                navController.navigate(R.id.programsFragment);
        }
        if (hasPunishment == 1){
            if ( navigate)
                navController.navigate(R.id.punishmentsFragment);

        }
        if (hasMusterTasks == 1) {
            if ( navigate)
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
        }
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
