package com.example.taskandconsequence.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.example.taskandconsequence.Helper;
import com.example.taskandconsequence.model.Backup;
import com.example.taskandconsequence.model.Program;
import com.example.taskandconsequence.model.ProgramOccurrence;
import com.example.taskandconsequence.model.Punishment;
import com.example.taskandconsequence.model.Status;
import com.example.taskandconsequence.model.Task;
import com.example.taskandconsequence.model.TaskOccurrence;
import com.example.taskandconsequence.views.demodata.DemoData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ProgramPactDB";

    // Table Names
    private static final String TABLE_PROGRAMS = "programs";
    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_TASK_OCCURRENCES = "task_occurrences";
    private static final String TABLE_PROGRAM_OCCURRENCES = "program_occurrences";
    private static final String TABLE_PUNISHMENTS = "punishments";

    // Common Column Names
    private static final String KEY_ID = "id";

    // Column names for PROGRAMS table
    private static final String KEY_PROGRAM_NAME = "name";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_START_DATE = "start_date";
    private static final String KEY_NUMBER_OF_PERIODS = "number_of_periods";
    private static final String KEY_SMALL_PUNISHMENT_ID = "small_punishment_id";
    private static final String KEY_BIG_PUNISHMENT_ID = "big_punishment_id";
    private static final String KEY_DONE = "done";

    // Column names for TASKS table
    private static final String KEY_TASK_DESCRIPTION = "description";
    private static final String KEY_TASK_NAME = "name";

    private static final String KEY_PROGRAM_ID = "program_id"; // Foreign key to PROGRAMS
    private static final String KEY_REWARDS = "rewards"; // Foreign key to PROGRAMS

    // Column names for PROGRAM_OCCURRENCES table
    private static final String KEY_DATE = "date";
    private static final String KEY_IS_COMPLETED = "is_completed";
    private static final String KEY_IS_PUNISHMENT_COMPLETED = "is_punishment_completed";

    // Column names for TASK_OCCURRENCES table
    private static final String KEY_TASK_ID = "task_id"; // Foreign key to TASKS

    private static final String KEY_PROGRAM_OCCURRENCE_ID = "program_occurrence_id";

    // Column names for PUNISHMENTS table
    private static final String KEY_NAME = "name";
    private static final String KEY_SEVERITY_LEVEL = "severity_level";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DEADLINE = "deadline";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PROGRAMS_TABLE = "CREATE TABLE " + TABLE_PROGRAMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_PROGRAM_NAME + " TEXT,"
                + KEY_CATEGORY + " TEXT,"
                + KEY_FREQUENCY + " TEXT,"
                + KEY_START_DATE + " INTEGER,"
                + KEY_NUMBER_OF_PERIODS + " INTEGER,"
                + KEY_SMALL_PUNISHMENT_ID + " INTEGER,"
                + KEY_BIG_PUNISHMENT_ID + " INTEGER,"
                + KEY_DONE + " INTEGER" + ")";

        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TASK_NAME + " TEXT,"
                + KEY_TASK_DESCRIPTION + " TEXT,"
                + KEY_PROGRAM_ID + " INTEGER,"
                + KEY_REWARDS + " INTEGER"
                + ")";

        String CREATE_PROGRAM_OCCURRENCES_TABLE = "CREATE TABLE " + TABLE_PROGRAM_OCCURRENCES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_PROGRAM_ID + " INTEGER,"
                + KEY_DATE + " INTEGER,"
                + KEY_IS_COMPLETED + " INTEGER,"
                + KEY_IS_PUNISHMENT_COMPLETED + " INTEGER" + ")";

        String CREATE_TASK_OCCURRENCES_TABLE = "CREATE TABLE " + TABLE_TASK_OCCURRENCES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TASK_ID + " INTEGER,"
                + KEY_PROGRAM_OCCURRENCE_ID + " INTEGER,"
                + KEY_DATE + " INTEGER,"
                + KEY_IS_COMPLETED + " INTEGER" + ")";

        String CREATE_PUNISHMENTS_TABLE = "CREATE TABLE " + TABLE_PUNISHMENTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_SEVERITY_LEVEL + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DEADLINE + " INTEGER" + ")";

        // Execute the SQL statements to create new tables
        db.execSQL(CREATE_PROGRAMS_TABLE);
        db.execSQL(CREATE_TASKS_TABLE);
        db.execSQL(CREATE_PROGRAM_OCCURRENCES_TABLE);
        db.execSQL(CREATE_TASK_OCCURRENCES_TABLE);
        db.execSQL(CREATE_PUNISHMENTS_TABLE);
//        db.close();
        List<Punishment> punishments = DemoData.getDemoPunishments();
        for (Punishment p : punishments)
            addPunishment(p, db);

        for (Task t : DemoData.getDemoTasks())
            addTaskInternal(t, db);

        for (Program p : DemoData.getDemoPrograms())
            addProgramInternal(p, db);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        // Create tables again
    }

    public long addProgram(Program program) {
        return addProgramInternal(program, null);
    }

    public long addProgramInternal(Program program, SQLiteDatabase db) {


        if (db == null)
            db = this.getWritableDatabase();

        Long smallPunishmentId = addPunishment(program.getSmallPunishment(),db);
        Long bigPunishmentId = addPunishment(program.getBigPunishment(),db);


        ContentValues values = new ContentValues();
        values.put(KEY_PROGRAM_NAME, program.getName());
        values.put(KEY_CATEGORY, program.getCategory());
        values.put(KEY_FREQUENCY, program.getFrequency());
        values.put(KEY_START_DATE, program.getStartDate().getTime());
        values.put(KEY_NUMBER_OF_PERIODS, program.getNumberOfPeriods());
        values.put(KEY_SMALL_PUNISHMENT_ID, smallPunishmentId);
        values.put(KEY_BIG_PUNISHMENT_ID, bigPunishmentId);
        values.put(KEY_DONE, program.getStatus().getValue());

        long id = db.insert(TABLE_PROGRAMS, null, values);

//        db.close();
        program.setId(id);
        for (Task task : program.getTasks()) {
            task.setProgramId(id);
            addTaskInternal(task,db);
        }
        int numOfDays = Helper. getNumOfDays(program.getFrequency());
        if(program.getProgramOccurrences() == null || program.getProgramOccurrences().size() == 0) {
            for (int i = 0; i < program.getNumberOfPeriods(); i++) {
                Date occurrenceDate = Helper.addDays(program.getStartDate(), numOfDays * i);
                Long programOccurrenceId = addProgramOccurrence(new ProgramOccurrence(null, program.getId(), occurrenceDate, Status.PENDING, Status.PENDING), db);
                for (Task task : program.getTasks()) {
                    addTaskOccurrence(new TaskOccurrence(null, task.getId(), occurrenceDate, Status.PENDING, programOccurrenceId), db);
                }
            }
        }else{
            for (ProgramOccurrence programOccurrence : program.getProgramOccurrences()) {
                programOccurrence.setProgramId(id);
                Long programOccurrenceId = addProgramOccurrence(programOccurrence, db);

                int index = 0;
                for (TaskOccurrence taskOccurrence : programOccurrence.getTaskOccurrences()) {
                    taskOccurrence.setProgramOccurrenceId(programOccurrenceId);
                    taskOccurrence.setTaskId(program.getTasks().get(index).getId());
                    addTaskOccurrence(taskOccurrence, db);
                    index++;
                }
            }
        }
        return id;
    }



    public List<Program> getAllProgramsSortedByStartDate() {
        List<Program> programs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_PROGRAMS + " ORDER BY "  + KEY_START_DATE + " DESC";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Program program = getProgramFromCursor(cursor);
                programs.add(program);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return programs;
    }

    public Program getProgram(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PROGRAMS, new String[]{KEY_ID, KEY_PROGRAM_NAME, KEY_CATEGORY, KEY_FREQUENCY, KEY_START_DATE, KEY_NUMBER_OF_PERIODS, KEY_SMALL_PUNISHMENT_ID, KEY_BIG_PUNISHMENT_ID, KEY_DONE},
                KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Program program = getProgramFromCursor(cursor);
        cursor.close();
        return program;
    }

    @NonNull
    private Program getProgramFromCursor(Cursor cursor) {
        Program program = new Program();
        program.setId(cursor.getLong(0));
        program.setName(cursor.getString(1));
        program.setCategory(cursor.getString(2));
        program.setFrequency(cursor.getString(3));
        program.setStartDate(new Date(cursor.getLong(4)));
        program.setNumberOfPeriods(cursor.getInt(5));
        // Assume getPunishmentById is a method to retrieve a Punishment by its ID
        program.setSmallPunishment(getPunishment(cursor.getLong(6)));
        program.setBigPunishment(getPunishment(cursor.getLong(7)));
        program.setStatus(Status.fromInt(cursor.getInt(8)));
        program.setTasks(getTasksForProgram(program.getId()));
        program.setProgramOccurrences(getProgramOccurrencesForProgram(program.getId()));
        program.setRewards(getRewardsForProgram(program.getId()));
        return program;
    }


    public int updateProgram(Program program) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (Task task : program.getTasks()) {
            task.setProgramId(program.getId());
            updateTask(task);
        }

        ContentValues values = new ContentValues();
        values.put(KEY_PROGRAM_NAME, program.getName());
        values.put(KEY_CATEGORY, program.getCategory());
        values.put(KEY_FREQUENCY, program.getFrequency());
        values.put(KEY_START_DATE, program.getStartDate().getTime());
        values.put(KEY_NUMBER_OF_PERIODS, program.getNumberOfPeriods());
        values.put(KEY_SMALL_PUNISHMENT_ID, program.getSmallPunishment().getId());
        values.put(KEY_BIG_PUNISHMENT_ID, program.getBigPunishment().getId());
        values.put(KEY_DONE, program.getStatus().getValue());

        return db.update(TABLE_PROGRAMS, values, KEY_ID + " = ?", new String[]{String.valueOf(program.getId())});
    }


    public void deleteProgram(long id) {



        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PROGRAMS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }


    public void deleteProgramWithEverything(Long programId) {
        Program program = getProgram(programId);
        deletePunishment(program.getSmallPunishment().getId());
        deletePunishment(program.getBigPunishment().getId());
        for (ProgramOccurrence programOccurrence: program.getProgramOccurrences()) {

            for (TaskOccurrence taskOccurrence : programOccurrence.getTaskOccurrences()) {
                deleteTaskOccurrence(taskOccurrence.getId());
            }
            deleteProgramOccurrence(programOccurrence.getId());

        }
        for (Task task : program.getTasks()) {
            deleteTask(task.getId());
        }
        deleteProgram(program.getId());
    }

    // CRUD : for Task table
    // ...

    public long addTask(Task task) {
        return addTaskInternal(task, null);
    }

    private long addTaskInternal(Task task, SQLiteDatabase db) {
        if (db == null)
            db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TASK_NAME, task.getName());
        values.put(KEY_TASK_DESCRIPTION, task.getDescription());
        values.put(KEY_PROGRAM_ID, task.getProgramId()); // Foreign key to Program
        values.put(KEY_REWARDS, task.getRewards());

        long id = db.insert(TABLE_TASKS, null, values);
//        db.close();
        task.setId(id);
        return id;
    }

    public List<Task> getTasksForProgram(long programId) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASKS, new String[]{KEY_ID,KEY_TASK_NAME, KEY_TASK_DESCRIPTION, KEY_PROGRAM_ID, KEY_REWARDS}, KEY_PROGRAM_ID + "=?", new String[]{String.valueOf(programId)}, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Task task = getTaskFromCursor(cursor);
                tasks.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return tasks;
    }

    public List<Task> getMusterTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASKS, new String[]{KEY_ID,KEY_TASK_NAME, KEY_TASK_DESCRIPTION, KEY_PROGRAM_ID, KEY_REWARDS}, KEY_PROGRAM_ID + "=?", new String[]{String.valueOf(-1)}, null, null, KEY_REWARDS + " Desc", null);

        if (cursor.moveToFirst()) {
            do {
                Task task = getTaskFromCursor(cursor);
                tasks.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return tasks;
    }

    @NonNull
    private Task getTaskFromCursor(Cursor cursor) {
        try {
            Task task = new Task();
            task.setId(cursor.getLong(0));
            task.setName(cursor.getString(1));
            task.setDescription(cursor.getString(2));
            task.setProgramId(cursor.getLong(3));
            task.setRewards(cursor.getInt(4));
            return task;
        }catch (CursorIndexOutOfBoundsException exception){
         return null;
        }
    }

    public Task getTask(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASKS, new String[]{KEY_ID,KEY_TASK_NAME, KEY_TASK_DESCRIPTION, KEY_PROGRAM_ID, KEY_REWARDS}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();
        Task task = getTaskFromCursor(cursor);

        cursor.close();
        return task;
    }


    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TASK_NAME, task.getName());
        values.put(KEY_TASK_DESCRIPTION, task.getDescription());
        values.put(KEY_PROGRAM_ID, task.getProgramId()); // Foreign key to Program
        values.put(KEY_REWARDS, task.getRewards());

        return db.update(TABLE_TASKS, values, KEY_ID + " = ?", new String[]{String.valueOf(task.getId())});
    }

    public void deleteTask(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
//        db.close();
    }


    // CRUD : for Punishment table
    // ...

    public List<Punishment> getPunishmentsWithNoDeadline() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Punishment> punishments = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_PUNISHMENTS + " WHERE " + KEY_DEADLINE + " = -1 " +
                "order by " + KEY_SEVERITY_LEVEL + " = 'small' Desc";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                punishments.add(getPunishmentFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return punishments;
    }

    public long addPunishment(Punishment punishment, SQLiteDatabase db) {
        if (db == null)
            db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, punishment.getName());
        values.put(KEY_SEVERITY_LEVEL, punishment.getSeverityLevel());
        values.put(KEY_DESCRIPTION, punishment.getDescription());
        values.put(KEY_DEADLINE, punishment.getDeadline());

        long id = db.insert(TABLE_PUNISHMENTS, null, values);
//        db.close();
        return id;
    }

    public Punishment getPunishment(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PUNISHMENTS, new String[]{KEY_ID, KEY_NAME, KEY_SEVERITY_LEVEL, KEY_DESCRIPTION, KEY_DEADLINE}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Punishment punishment = getPunishmentFromCursor(cursor);

        cursor.close();
        return punishment;
    }

    @NonNull
    private Punishment getPunishmentFromCursor(Cursor cursor) {
        Punishment punishment = new Punishment();
        punishment.setId(cursor.getLong(0));
        punishment.setName(cursor.getString(1));
        punishment.setSeverityLevel(cursor.getString(2));
        punishment.setDescription(cursor.getString(3));
        punishment.setDeadline(cursor.getInt(4));
        return punishment;
    }

    public int updatePunishment(Punishment punishment) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, punishment.getName());
        values.put(KEY_SEVERITY_LEVEL, punishment.getSeverityLevel());
        values.put(KEY_DESCRIPTION, punishment.getDescription());
        values.put(KEY_DEADLINE, punishment.getDeadline());

        return db.update(TABLE_PUNISHMENTS, values, KEY_ID + " = ?", new String[]{String.valueOf(punishment.getId())});
    }

    public void deletePunishment(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PUNISHMENTS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
//        db.close();
    }

    // CRUD : for ProgramOccurrence table
    // ...

    public long addProgramOccurrence(ProgramOccurrence occurrence, SQLiteDatabase db) {
        if ( db == null)
         db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROGRAM_ID, occurrence.getProgramId());
        values.put(KEY_DATE, occurrence.getDate().getTime());
        values.put(KEY_IS_COMPLETED, occurrence.getStatus().getValue());
        values.put(KEY_IS_PUNISHMENT_COMPLETED, occurrence.getPunishmentStatus().getValue());

        long id = db.insert(TABLE_PROGRAM_OCCURRENCES, null, values);
//        db.close();
        return id;
    }

    public List<ProgramOccurrence> getProgramOccurrencesForProgram(long programId) {
        List<ProgramOccurrence> occurrences = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PROGRAM_OCCURRENCES,
                new String[]{KEY_ID, KEY_PROGRAM_ID, KEY_DATE, KEY_IS_COMPLETED, KEY_IS_PUNISHMENT_COMPLETED},
                KEY_PROGRAM_ID + "=?", new String[]{String.valueOf(programId)}, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                ProgramOccurrence occurrence = getProgramOccurrenceFromCursor(cursor);

                occurrences.add(occurrence);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return occurrences;
    }


    public List<ProgramOccurrence> getProgramOccurrencesToday() {
        List<ProgramOccurrence> occurrences = new ArrayList<>();
        Date endDate = new Date();
        Date startDate = Helper.addDays(endDate, -1);
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT oc.id ,oc.program_id,oc.date ,oc.is_completed ," +
                " oc.is_punishment_completed FROM " + TABLE_PROGRAM_OCCURRENCES + " oc " +
                " INNER JOIN "+ TABLE_PROGRAMS + " p " +
                                " on p.id = oc.program_id   " +
                " WHERE oc.date >= ? AND oc.date <= ? AND p.frequency = 'daily'";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(startDate.getTime()), String.valueOf(endDate.getTime())});

        if (cursor.moveToFirst()) {
            do {
                ProgramOccurrence occurrence = getProgramOccurrenceFromCursor(cursor);
                occurrence.setProgram(getProgram(occurrence.getProgramId()));
                occurrences.add(occurrence);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return occurrences;
    }
    
    public List<ProgramOccurrence> getPendingProgramOccurrencesToday() {
        List<ProgramOccurrence> occurrences = new ArrayList<>();
        Date endDate = new Date();
        Date startDate = Helper.addDays(endDate, -1);
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT oc.id ,oc.program_id,oc.date ,oc.is_completed ," +
                " oc.is_punishment_completed FROM " + TABLE_PROGRAM_OCCURRENCES + " oc " +
                " INNER JOIN "+ TABLE_PROGRAMS + " p " +
                                " on p.id = oc.program_id   " +
                " WHERE oc.is_completed = ? AND oc.date >= ? AND oc.date <= ? AND p.frequency = 'daily'";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(Status.PENDING.getValue()), String.valueOf(startDate.getTime()), String.valueOf(endDate.getTime())});

        if (cursor.moveToFirst()) {
            do {
                ProgramOccurrence occurrence = getProgramOccurrenceFromCursor(cursor);
                occurrence.setProgram(getProgram(occurrence.getProgramId()));
                occurrences.add(occurrence);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return occurrences;
    }
    public List<ProgramOccurrence> getPendingPunishmentProgramOccurrencesToday() {
        List<ProgramOccurrence> occurrences = new ArrayList<>();
        Date endDate = Helper.addDays(new Date(), -1) ;
        Date startDate = Helper.addDays(endDate, -1) ;
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT oc.id ,oc.program_id,oc.date ,oc.is_completed ," +
                " oc.is_punishment_completed FROM " + TABLE_PROGRAM_OCCURRENCES + " oc " +
                " INNER JOIN "+ TABLE_PROGRAMS + " p " +
                                " on p.id = oc.program_id   " +
                " WHERE oc.is_completed = ? AND oc.date >= ? AND oc.date <= ? AND p.frequency = 'daily'";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(Status.PENDING_PUNISHMENT.getValue()), String.valueOf(startDate.getTime()), String.valueOf(endDate.getTime())});

        if (cursor.moveToFirst()) {
            do {
                ProgramOccurrence occurrence = getProgramOccurrenceFromCursor(cursor);
                occurrence.setProgram(getProgram(occurrence.getProgramId()));
                occurrences.add(occurrence);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return occurrences;
    }

    public List<ProgramOccurrence> getProgramOccurrencesThisWeek() {
        List<ProgramOccurrence> occurrences = new ArrayList<>();
        Date endDate = new Date();
        Date startDate = Helper.addDays(endDate, -7);
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT oc.id ,oc.program_id,oc.date ,oc.is_completed ," +
                " oc.is_punishment_completed FROM " + TABLE_PROGRAM_OCCURRENCES + " oc " +
                " INNER JOIN "+ TABLE_PROGRAMS + " p " +
                " on p.id = oc.program_id   " +
                " WHERE oc.date >= ? AND oc.date <= ? AND p.frequency = 'weekly'";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(startDate.getTime()), String.valueOf(endDate.getTime())});

        if (cursor.moveToFirst()) {
            do {
                ProgramOccurrence occurrence = getProgramOccurrenceFromCursor(cursor);
                occurrence.setProgram(getProgram(occurrence.getProgramId()));
                occurrences.add(occurrence);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return occurrences;
        }

    public List<ProgramOccurrence> getProgramOccurrencesThisMonth() {
        List<ProgramOccurrence> occurrences = new ArrayList<>();
        Date endDate = new Date();
        Date startDate = Helper.addDays(endDate, -30);
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT oc.id ,oc.program_id,oc.date ,oc.is_completed ," +
                " oc.is_punishment_completed FROM " + TABLE_PROGRAM_OCCURRENCES + " oc " +
                " INNER JOIN "+ TABLE_PROGRAMS + " p " +
                " on p.id = oc.program_id   " +
                " WHERE oc.date >= ? AND oc.date <= ? AND p.frequency = 'monthly'";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(startDate.getTime()), String.valueOf(endDate.getTime())});

        if (cursor.moveToFirst()) {
            do {
                ProgramOccurrence occurrence = getProgramOccurrenceFromCursor(cursor);
                occurrence.setProgram(getProgram(occurrence.getProgramId()));
                occurrences.add(occurrence);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return occurrences;
        }


    public ProgramOccurrence getProgramOccurrence(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PROGRAM_OCCURRENCES, new String[]{KEY_ID, KEY_PROGRAM_ID, KEY_DATE, KEY_IS_COMPLETED, KEY_IS_PUNISHMENT_COMPLETED}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        ProgramOccurrence occurrence = getProgramOccurrenceFromCursor(cursor);
        occurrence.setProgram(getProgram(occurrence.getProgramId()));
        cursor.close();
        return occurrence;
    }

    @NonNull
    private ProgramOccurrence getProgramOccurrenceFromCursor(Cursor cursor) {
        ProgramOccurrence occurrence = new ProgramOccurrence();
        occurrence.setId(cursor.getLong(0));
        occurrence.setProgramId(cursor.getLong(1));
        occurrence.setDate(new Date(cursor.getLong(2)));
        occurrence.setStatus(Status.fromInt(cursor.getInt(3)));
        occurrence.setPunishmentStatus(Status.fromInt(cursor.getInt(4)));
        occurrence.setTaskOccurrences(getAllTaskOccurrencesForProgramOccurrence(occurrence.getId()));
        occurrence.setRewards(getRewardsForProgramOccurrence(occurrence.getId()));
        return occurrence;
    }

    public int updateProgramOccurrence(ProgramOccurrence occurrence) {


        SQLiteDatabase db = this.getWritableDatabase();

        for (TaskOccurrence taskOccurrence : occurrence.getTaskOccurrences())
            updateTaskOccurrence(taskOccurrence);
        ContentValues values = new ContentValues();
        values.put(KEY_PROGRAM_ID, occurrence.getProgramId());
        values.put(KEY_DATE, occurrence.getDate().getTime());
        values.put(KEY_IS_COMPLETED, occurrence.getStatus().getValue());
        values.put(KEY_IS_PUNISHMENT_COMPLETED, occurrence.getPunishmentStatus().getValue());

        return db.update(TABLE_PROGRAM_OCCURRENCES, values, KEY_ID + " = ?", new String[]{String.valueOf(occurrence.getId())});
    }

    public void deleteProgramOccurrence(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PROGRAM_OCCURRENCES, KEY_ID + " = ?", new String[]{String.valueOf(id)});
//        db.close();
    }


    // CRUD : for TaskOccurrence table
    // ...

    public long addTaskOccurrence(TaskOccurrence occurrence, SQLiteDatabase db) {
        if ( db == null)
             db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TASK_ID, occurrence.getTaskId());
        values.put(KEY_PROGRAM_OCCURRENCE_ID, occurrence.getProgramOccurrenceId());
        values.put(KEY_DATE, occurrence.getDate().getTime());
        values.put(KEY_IS_COMPLETED, occurrence.getStatus().getValue());

        long id = db.insert(TABLE_TASK_OCCURRENCES, null, values);
//        db.close();
        return id;
    }


    public List<TaskOccurrence> getAllTaskOccurrencesForProgramOccurrence(long programOccurrenceId) {
        List<TaskOccurrence> taskOccurrences = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_TASK_OCCURRENCES + " WHERE " + KEY_PROGRAM_OCCURRENCE_ID + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(programOccurrenceId)});

        // Looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TaskOccurrence occurrence = getTaskOccurrenceFromCursor(cursor);
                taskOccurrences.add(occurrence);
            } while (cursor.moveToNext());
        }

        // Close cursor
        cursor.close();

        // return task occurrence list
        return taskOccurrences;
    }

    @NonNull
    private TaskOccurrence getTaskOccurrenceFromCursor(Cursor cursor) {
        TaskOccurrence occurrence = new TaskOccurrence();
        occurrence.setId(cursor.getLong(0));
        occurrence.setTaskId(cursor.getLong(1));
        occurrence.setProgramOccurrenceId(cursor.getLong(2));
        occurrence.setDate(new Date(cursor.getLong(3)));
        occurrence.setStatus(Status.fromInt(cursor.getInt(4)));
        return occurrence;
    }

    public TaskOccurrence getTaskOccurrence(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASK_OCCURRENCES, new String[]{KEY_ID, KEY_TASK_ID, KEY_PROGRAM_OCCURRENCE_ID, KEY_DATE, KEY_IS_COMPLETED}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        TaskOccurrence occurrence = getTaskOccurrenceFromCursor(cursor);

        cursor.close();
        return occurrence;
    }


    public int updateTaskOccurrence(TaskOccurrence occurrence) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TASK_ID, occurrence.getTaskId());
        values.put(KEY_PROGRAM_OCCURRENCE_ID, occurrence.getProgramOccurrenceId());
        values.put(KEY_DATE, occurrence.getDate().getTime());
        values.put(KEY_IS_COMPLETED, occurrence.getStatus().getValue());

        return db.update(TABLE_TASK_OCCURRENCES, values, KEY_ID + " = ?", new String[]{String.valueOf(occurrence.getId())});
    }

    public void deleteTaskOccurrence(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASK_OCCURRENCES, KEY_ID + " = ?", new String[]{String.valueOf(id)});
//        db.close();
    }

    // Crud : calculate rewards

    public Integer getRewardsForProgramOccurrence(long programOccurrenceId) {
        List<TaskOccurrence> taskOccurrences = new ArrayList<>();
        String selectQuery = "SELECT sum( tasks.rewards ) FROM  " + TABLE_PROGRAM_OCCURRENCES +
                " INNER JOIN " + TABLE_TASK_OCCURRENCES +
                " ON  program_occurrences.id = task_occurrences.program_occurrence_id" +
                " INNER JOIN " + TABLE_TASKS +
                " ON  tasks.id = task_occurrences.task_id" +


                " WHERE program_occurrences.id = ?" +
                "and task_occurrences.is_completed = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(programOccurrenceId),String.valueOf(Status.SUCCEED.getValue())});
        int rewards = 0;
        if (cursor != null && cursor.moveToFirst()) {
            rewards = cursor.getInt(0);
        }
        if (cursor != null) {
            cursor.close();
        }
        return rewards;
    }

    public Integer getRewardsForProgram(long programId) {
        List<TaskOccurrence> taskOccurrences = new ArrayList<>();

        String selectQuery = "SELECT sum( tasks.rewards ) FROM  " + TABLE_PROGRAM_OCCURRENCES +
                " INNER JOIN " + TABLE_TASK_OCCURRENCES +
                " ON  program_occurrences.id = task_occurrences.program_occurrence_id" +
                " INNER JOIN " + TABLE_TASKS +
                " ON  tasks.id = task_occurrences.task_id" +
                " INNER JOIN " + TABLE_PROGRAMS +
                " ON  programs.id = program_occurrences." + KEY_PROGRAM_ID +


                " WHERE programs.id = ?" +
                " and task_occurrences.is_completed = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(programId),String.valueOf(Status.SUCCEED.getValue())});
        int rewards = 0;
        if (cursor != null && cursor.moveToFirst()) {
            rewards = cursor.getInt(0);
        }
        if (cursor != null) {
            cursor.close();
        }
        return rewards;
    }

    public Integer getAllReward() {
        List<TaskOccurrence> taskOccurrences = new ArrayList<>();
        String selectQuery = "SELECT sum( tasks.rewards ) FROM  " + TABLE_TASK_OCCURRENCES +
                " INNER JOIN " + TABLE_TASKS +
                " ON  tasks.id = task_occurrences.task_id" +
                " WHERE  task_occurrences.is_completed =  ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(Status.SUCCEED.getValue())});
        int rewards = 0;
        if (cursor != null && cursor.moveToFirst()) {
            rewards = cursor.getInt(0);
        }
        if (cursor != null) {
            cursor.close();
        }
        return rewards;
    }

    public Integer getRewardForTaskOccurrence(Long taskOccurrence_id) {
        List<TaskOccurrence> taskOccurrences = new ArrayList<>();
        String selectQuery = "SELECT sum( tasks.rewards ) FROM  " + TABLE_TASK_OCCURRENCES +
                " INNER JOIN " + TABLE_TASKS +
                " ON  tasks.id = task_occurrences.task_id" +
                " WHERE  Task_occurrences.is_completed = 1 and " +
                " task_occurrences.id = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(taskOccurrence_id)});
        int rewards = 0;
        if (cursor != null && cursor.moveToFirst()) {
            rewards = cursor.getInt(0);
        }
        if (cursor != null) {
            cursor.close();
        }
        return rewards;
    }
    //**************************************
    //**************************************
    public Backup getAllBackup() {
        Backup backup = new Backup();
        backup.setTasks(getMusterTasks());
        backup.setPunishments(getPunishmentsWithNoDeadline());
        backup.setPrograms(getAllProgramsSortedByStartDate());
        return backup;
    }
    
    public boolean addBackupFile(Backup backup){
        for (Program program :  backup.getPrograms())
            addProgram(program);
        for (Task task :  backup.getTasks())
            addTask(task);
        for (Punishment punishment :  backup.getPunishments())
            addPunishment(punishment,null);
        return true;
    }

    public boolean deleteAllPrograms(){
        List<Program> allProgram = getAllProgramsSortedByStartDate();
        for ( Program p : allProgram)
            deleteProgramWithEverything(p.getId());
        return true;
    }

    public boolean deleteAllPunishments(){
        List<Punishment> allPunishment = getPunishmentsWithNoDeadline();
        for ( Punishment p : allPunishment)
            deletePunishment(p.getId());
        return true;
    }

    public boolean deleteAllTasks(){
        List<Task> allTasks = getMusterTasks();
        for ( Task task : allTasks)
            deleteTask(task.getId());
        return true;
    }
}
