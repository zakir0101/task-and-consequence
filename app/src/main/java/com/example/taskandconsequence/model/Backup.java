package com.example.taskandconsequence.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Backup {
    private List<Program> programs = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();
    private List<Punishment> punishments = new ArrayList<>();


    public Backup() {
    }

    public List<Program> getPrograms() {
        return programs;
    }

    public void setPrograms(List<Program> programs) {
        this.programs = programs;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Punishment> getPunishments() {
        return punishments;
    }

    public void setPunishments(List<Punishment> punishments) {
        this.punishments = punishments;
    }


    public static Backup fromJson(JSONObject jsonObject) throws JSONException {
        Backup backup = new Backup();

        // Handle programs
        JSONArray programsArray = jsonObject.optJSONArray("programs");
        List<Program> programs = new ArrayList<>();
        if (programsArray != null) {
            for (int i = 0; i < programsArray.length(); i++) {
                programs.add(Program.fromJson(programsArray.getJSONObject(i)));
            }
        }
        backup.setPrograms(programs);

        // Handle tasks
        JSONArray tasksArray = jsonObject.optJSONArray("tasks");
        List<Task> tasks = new ArrayList<>();
        if (tasksArray != null) {
            for (int i = 0; i < tasksArray.length(); i++) {
                tasks.add(Task.fromJson(tasksArray.getJSONObject(i)));
            }
        }
        backup.setTasks(tasks);

        // Handle punishments
        JSONArray punishmentsArray = jsonObject.optJSONArray("punishments");
        List<Punishment> punishments = new ArrayList<>();
        if (punishmentsArray != null) {
            for (int i = 0; i < punishmentsArray.length(); i++) {
                punishments.add(Punishment.fromJson(punishmentsArray.getJSONObject(i)));
            }
        }
        backup.setPunishments(punishments);

        return backup;
    }


    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            // Handling programs
            JSONArray programsArray = new JSONArray();
            for (Program program : programs) {
                programsArray.put(program.toJson());
            }
            jsonObject.put("programs", programsArray);

            // Handling tasks
            JSONArray tasksArray = new JSONArray();
            for (Task task : tasks) {
                tasksArray.put(task.toJson());
            }
            jsonObject.put("tasks", tasksArray);

            // Handling punishments
            JSONArray punishmentsArray = new JSONArray();
            for (Punishment punishment : punishments) {
                punishmentsArray.put(punishment.toJson());
            }
            jsonObject.put("punishments", punishmentsArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}
