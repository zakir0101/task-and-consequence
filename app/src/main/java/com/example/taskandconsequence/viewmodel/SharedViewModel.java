package com.example.taskandconsequence.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskandconsequence.db.DatabaseHelper;
import com.example.taskandconsequence.model.Backup;
import com.example.taskandconsequence.model.Program;
import com.example.taskandconsequence.model.ProgramOccurrence;
import com.example.taskandconsequence.model.Punishment;
import com.example.taskandconsequence.model.Task;
import com.example.taskandconsequence.model.TaskOccurrence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SharedViewModel extends ViewModel {

    private final DatabaseHelper databaseHelper;
    private final ExecutorService executor;
    public boolean programMode = false;


    private MutableLiveData<Integer> allReward = new MutableLiveData<>(0);
    private MutableLiveData<List<Program>> alLProgram = new MutableLiveData<>(new ArrayList<>());
    MutableLiveData<List<Task>> allTasks = new MutableLiveData<>(new ArrayList<>());


    MutableLiveData<List<Task>> programTasks = new MutableLiveData<>(new ArrayList<>());
    MutableLiveData<List<Punishment>> allPunishments = new MutableLiveData<>(new ArrayList<>());

    public Set<Long> selectedItems = new HashSet<>();

    public boolean editMode = false;
    public boolean taskEditMode = false;


    public Object activeObject = null;
    public MutableLiveData<Program> activeProgram = new MutableLiveData<Program>();
    public Object activeTask = null;
    private MutableLiveData<ProgramOccurrence> activeOccurrence = new MutableLiveData<>();
    public Long activeOccurrenceId = null;
    public boolean isSelectionModeActive = false;

    public Backup backup;

    public SharedViewModel(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.executor = Executors.newSingleThreadExecutor();

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }


    public void resetSelection() {
        selectedItems = new HashSet<>();
        isSelectionModeActive = false;
    }


    public void startDestinationSetup() {
        activeTask = null;
        activeObject = null;
        editMode = false;
        programMode = false;
        resetSelection();
    }


    // Add methods for database operations that can be called by UI controllers


    public LiveData<Long> addProgram(Program program) {
        MutableLiveData<Long> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            long id = databaseHelper.addProgram(program);
//            liveData.postValue(id);
        });
        return liveData;
    }

    public LiveData<List<Program>> getAllPrograms() {
        updateAllProgramsSortedByStartDate();
        return alLProgram;
    }

    private void updateAllProgramsSortedByStartDate() {
        executor.execute(() -> {
            List<Program> programs = databaseHelper.getAllProgramsSortedByStartDate();
            alLProgram.postValue(programs);
        });
    }

    public LiveData<Program> getActiveProgram(long id) {
        updateActiveProgram();
        return activeProgram;
    }

    private void updateActiveProgram() {
        if (activeObject != null && activeObject instanceof Program) {
            executor.execute(() -> {
                Program program = databaseHelper.getProgram(((Program) activeObject).getId());
                activeProgram.postValue(program);
                activeObject = program;
            });
        }
    }

    public void updateProgram(Program program) {
        executor.execute(() -> {
            int result = databaseHelper.updateProgram(program);
//            updateAllProgramsSortedByStartDate();
            updateActiveProgram();
        });
    }

    public void updateProgramOnly(Program program) {
        executor.execute(() -> {
            int result = databaseHelper.updateProgram(program);
        });
    }

    public void deleteProgram(Long id) {
        executor.execute(() -> {
            databaseHelper.deleteProgramWithEverything(id);
            updateAllProgramsSortedByStartDate();
//            updateActiveProgram();
        });

    }

//***********************************************
//***********************************************
//***********************************************

    public LiveData<List<Task>> getProgramTasks() {
        return programTasks;
    }

    public void addProgramTasks(Task programTask) {
        List<Task> tasks = programTasks.getValue();
        tasks.add(programTask);
        this.programTasks.postValue(tasks);
    }

    public void addAllProgramTasks(List<Task> programTask) {
        List<Task> tasks = programTasks.getValue();
        tasks.addAll(programTask);
        this.programTasks.postValue(tasks);
    }

    public void removeProgramTasks(Task programTask) {
        List<Task> tasks = programTasks.getValue();
        tasks.remove(programTask);
        this.programTasks.postValue(tasks);
    }

    public void clearProgramTasks() {
        this.programTasks.setValue(new ArrayList<>());
    }

    public void addTask(Task task) {
        executor.execute(() -> {
            long id = databaseHelper.addTask(task);
            updateMusterTasks();

        });
    }

    public LiveData<List<Task>> getMusterTasks() {
        updateMusterTasks();
        return allTasks;
    }

    public void updateMusterTasks() {
        executor.execute(() -> {
            List<Task> tasks = databaseHelper.getMusterTasks();
            allTasks.postValue(tasks);
        });
    }

    public LiveData<Task> getTask(long id) {
//        if (id == 0)
//            throw new IllegalArgumentException();
        MutableLiveData<Task> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            Task task = databaseHelper.getTask(id);
            liveData.postValue(task);
        });
        return liveData;
    }

    public void updateTask(Task task) {
        executor.execute(() -> {
            int result = databaseHelper.updateTask(task);
            updateMusterTasks();
        });
    }

    public void deleteTask(long id) {
        executor.execute(() -> {
            databaseHelper.deleteTask(id);
            updateMusterTasks();
        });
    }


    // *********************************************
    // *********************************************

    public LiveData<List<Punishment>> getMusterPunishments() {
        updateMusterPunishments();
        return allPunishments;
    }

    public void updateMusterPunishments() {
        executor.execute(() -> {
            List<Punishment> punishments = databaseHelper.getPunishmentsWithNoDeadline();
            allPunishments.postValue(punishments);
        });
    }

    public void addPunishment(Punishment punishment) {
        executor.execute(() -> {
            long id = databaseHelper.addPunishment(punishment, null);
            updateMusterPunishments();
        });
    }

    public LiveData<Punishment> getPunishment(long id) {
        MutableLiveData<Punishment> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            Punishment punishment = databaseHelper.getPunishment(id);
            liveData.postValue(punishment);
        });
        return liveData;
    }

    public void updatePunishment(Punishment punishment) {
        executor.execute(() -> {
            int result = databaseHelper.updatePunishment(punishment);
            updateMusterPunishments();
        });
    }

    public void deletePunishment(long id) {
        executor.execute(() -> {
            databaseHelper.deletePunishment(id);
            updateMusterPunishments();
        });
    }

    //********************************************************
    //********************************************************

    public LiveData<ProgramOccurrence> getActiveOccurrence() {
        updateActiveOccurrence();
        return activeOccurrence;
    }

    private void updateActiveOccurrence() {
        if (activeOccurrenceId != null) {
            executor.execute(() -> {
                ProgramOccurrence programOccurrence = databaseHelper.getProgramOccurrence(activeOccurrenceId);
                activeOccurrence.postValue(programOccurrence);
            });
        }
    }


    public void updateProgramOccurrence(ProgramOccurrence programOccurrence) {
        executor.execute(() -> {
            int result = databaseHelper.updateProgramOccurrence(programOccurrence);
            updateActiveOccurrence();
        });
    }

    public void updateProgramOccurrenceOnly(ProgramOccurrence programOccurrence) {
        executor.execute(() -> {
            int result = databaseHelper.updateProgramOccurrence(programOccurrence);
        });
    }


    public LiveData<List<ProgramOccurrence>> getProgramOccurrenceToday() {
        MutableLiveData<List<ProgramOccurrence>> occurrences = new MutableLiveData<>();
        executor.execute(() -> {
            occurrences.postValue(databaseHelper.getProgramOccurrencesToday());
        });
        return occurrences;
    }

    public LiveData<List<ProgramOccurrence>> getProgramOccurrenceThisWeek() {
        MutableLiveData<List<ProgramOccurrence>> occurrences = new MutableLiveData<>();
        executor.execute(() -> {
            occurrences.postValue(databaseHelper.getProgramOccurrencesThisWeek());
        });
        return occurrences;
    }

    public LiveData<List<ProgramOccurrence>> getProgramOccurrenceThisMonth() {
        MutableLiveData<List<ProgramOccurrence>> occurrences = new MutableLiveData<>();
        executor.execute(() -> {
            occurrences.postValue(databaseHelper.getProgramOccurrencesThisMonth());
        });
        return occurrences;
    }
    //********************************************************
    //********************************************************


    public void updateTaskOccurrence(TaskOccurrence taskOccurrence) {
        executor.execute(() -> {
            int result = databaseHelper.updateTaskOccurrence(taskOccurrence);
            updateActiveOccurrence();
        });
    }

    //******************************************************++
    //******************************************************++

    public LiveData<Integer> getAllReward() {
        updateAllReward();
        return allReward;
    }

    private void updateAllReward() {
        executor.execute(() -> {
            int result = databaseHelper.getAllReward();
            allReward.postValue(result);
        });
    }

    public LiveData<Backup> getAllBackup() {
        MutableLiveData<Backup> allBackup = new MutableLiveData<>();
        executor.execute(() -> {
            Backup backup = databaseHelper.getAllBackup();
            allBackup.postValue(backup);
        });
        return allBackup;
    }

    public void deleteAllPrograms() {
        executor.execute(databaseHelper::deleteAllPrograms);
    }

    public void deleteAllTasks() {
        executor.execute(databaseHelper::deleteAllTasks);
    }

    public void deleteAllPunishments() {
        executor.execute(databaseHelper::deleteAllPunishments);
    }

    public LiveData<Boolean> loadBackup(Backup backup) {
        MutableLiveData<Boolean> done = new MutableLiveData<>();
        executor.execute(() -> {
            databaseHelper.addBackupFile(backup);
            updateAllProgramsSortedByStartDate();
            updateMusterTasks();
            updateMusterPunishments();
            done.postValue(true);
        });
        return done;
    }
}
