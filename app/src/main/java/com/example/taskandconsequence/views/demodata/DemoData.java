package com.example.taskandconsequence.views.demodata;

import com.example.taskandconsequence.model.Program;
import com.example.taskandconsequence.model.Punishment;
import com.example.taskandconsequence.model.Status;
import com.example.taskandconsequence.model.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DemoData {

    public static List<Punishment> getDemoPunishments() {
        List<Punishment> punishments = new ArrayList<>();

        punishments.add(new Punishment(1L, "Late Submission", "small", "This punishment is for submitting assignments later than the due date. It requires extra effort to catch up on missed deadlines.", -1));
        punishments.add(new Punishment(2L, "Missed Workout", "small", "Assigned for skipping a planned workout session. It aims to encourage maintaining a regular exercise routine.", -1));
        punishments.add(new Punishment(3L, "Overeating", "small", "This occurs when exceeding the daily calorie limit. It's a reminder to maintain healthy eating habits.", -1));
        punishments.add(new Punishment(4L, "Sleeping Late", "big", "For going to bed later than the set time. It emphasizes the importance of a regular sleep schedule for overall well-being.", -1));
        punishments.add(new Punishment(5L, "Missing Meetings", "big", "This punishment is applied for failing to attend scheduled meetings, emphasizing the importance of commitment and time management.", -1));
        punishments.add(new Punishment(6L, "Procrastination", "big", "Targeted at repeatedly postponing tasks. It aims to promote timely completion of responsibilities.", -1));

        return punishments;
    }

    public static List<Task> getDemoTasks() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task("Java programming", "Explore Java programming fundamentals, focusing on object-oriented concepts.", 10, -1L));
        tasks.add(new Task("Design a user-friendly", "Design a user-friendly interface for a mobile app, emphasizing intuitive navigation.", 20, -1L));
        tasks.add(new Task("Market analysis", "Conduct market analysis for an upcoming product, identifying key trends.", 15, -1L));
        tasks.add(new Task("Develop database", "Develop a small-scale database using SQL, practicing data management techniques.", 25, -1L));
        tasks.add(new Task("Create a presentation", "Create a presentation on modern web technologies, covering HTML5, CSS3, and JavaScript.", 18, -1L));
        tasks.add(new Task("machine learning", "Implement a basic machine learning model using Python and analyze its accuracy.", 30, -1L));
        tasks.add(new Task("Write a report", "Write a report on the impacts of climate change, focusing on sustainable practices.", 22, -1L));
        tasks.add(new Task("Organize a community", "Organize a community event that promotes digital literacy and collect feedback.", 35, -1L));
        return tasks;
    }


    public static List<Program> getDemoPrograms() {
        // Create some demo tasks
        List<Task> allTasks = getDemoTasks();
        List<Punishment> allPunishment = getDemoPunishments();


        // Create demo programs
        Program activeProgram1 = new Program(1L, "Active Program 1", "daily", new Date(), 30,getRandomPunishment(),getRandomPunishment(), Status.PENDING, getRandomTasks(1L));
        Program activeProgram2 = new Program(2L, "Active Program 2", "weekly", new Date(), 12,getRandomPunishment(),getRandomPunishment(), Status.PENDING , getRandomTasks(2L));

        Program pendingPunishmentProgram1 = new Program(3L, "Pending Punishment Program 1", "monthly", new Date(), 5, getRandomPunishment(),getRandomPunishment(), Status.PENDING_PUNISHMENT,getRandomTasks(3L));
        Program pendingPunishmentProgram2 = new Program(4L, "Pending Punishment Program 2", "weekly", new Date(), 10, getRandomPunishment(),getRandomPunishment(), Status.PENDING_PUNISHMENT,getRandomTasks(4L));

        Program succeedProgram1 = new Program(5L, "Succeed Program 1", "daily", new Date(), 20, getRandomPunishment(),getRandomPunishment(), Status.SUCCEED,getRandomTasks(5L));
        Program succeedProgram2 = new Program(6L, "Succeed Program 2", "monthly", new Date(), 6,getRandomPunishment(),getRandomPunishment(), Status.SUCCEED,getRandomTasks(6L));

        Program failedProgram1 = new Program(7L, "Failed Program 1", "weekly", new Date(), 8, getRandomPunishment(),getRandomPunishment(),  Status.FAIL ,getRandomTasks(7L));
        Program failedProgram2 = new Program(8L, "Failed Program 2", "daily", new Date(), 15, getRandomPunishment(),getRandomPunishment(), Status.FAIL, getRandomTasks(8L));

        return Arrays.asList(activeProgram1, activeProgram2, pendingPunishmentProgram1, pendingPunishmentProgram2, succeedProgram1, succeedProgram2, failedProgram1, failedProgram2);
    }


    public static List<Task> getRandomTasks( Long programId) {
        List<Task> tasks = getDemoTasks();
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }

        Random random = new Random();
        int subsetSize = random.nextInt(tasks.size()) + 1; // +1 to ensure at least one task is selected

        List<Task> shuffledTasks = new ArrayList<>(tasks);
        Collections.shuffle(shuffledTasks);

        shuffledTasks = shuffledTasks.subList(0, Math.min(subsetSize, shuffledTasks.size()));
        for (Task t : shuffledTasks) {
            t.setId(null);
            t.setProgramId(programId);
        }
        return shuffledTasks;
    }

    public static Punishment getRandomPunishment() {
        List<Punishment> punishments = getDemoPunishments();
        Random random = new Random();
        int randomIndex = random.nextInt(punishments.size()); // +1 to ensure at least one task is selected
        Punishment selected = punishments.get(randomIndex);
        selected.setId(null);
        selected.setDeadline(1);
        return selected;
    }
}
