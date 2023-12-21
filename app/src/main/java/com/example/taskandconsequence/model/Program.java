package com.example.taskandconsequence.model;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Program {
    private Long id;
    private String category;
    private String name;
    private String frequency; // "daily" , "weekly" or "monthly"
    private Date startDate;
    private int numberOfPeriods;
    private List<Task> tasks;

    private Punishment smallPunishment; // Reference to a Punishment object
    private Punishment bigPunishment;   // Reference to a Punishment object

    public List<ProgramOccurrence> programOccurrences ;
    private Status status; // Indicates if the program is completed or broken


    private  Integer rewards;

    public Program() {
        tasks = new ArrayList<>();
    }

    public Program(Long id, String name,  String frequency, Date startDate, int numberOfPeriods, Punishment smallPunishment, Punishment bigPunishment, Status status, List<Task> tasks) {
        this.id = id;
        this.name = name;
        this.category = "category";
        this.frequency = frequency;
        this.startDate = startDate;
        this.numberOfPeriods = numberOfPeriods;
        this.smallPunishment = smallPunishment;
        this.bigPunishment = bigPunishment;
        this.status = status;
        this.tasks = tasks;
    }

    public List<ProgramOccurrence> getProgramOccurrences() {
        return programOccurrences;
    }

    public void setProgramOccurrences(List<ProgramOccurrence> programOccurrences) {
        this.programOccurrences = programOccurrences;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public int getNumberOfPeriods() {
        return numberOfPeriods;
    }

    public void setNumberOfPeriods(int numberOfPeriods) {
        this.numberOfPeriods = numberOfPeriods;
    }

    public Punishment getSmallPunishment() {
        return smallPunishment;
    }

    public void setSmallPunishment(Punishment smallPunishment) {
        this.smallPunishment = smallPunishment;
    }

    public Punishment getBigPunishment() {
        return bigPunishment;
    }

    public void setBigPunishment(Punishment bigPunishment) {
        this.bigPunishment = bigPunishment;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }


    public Integer getRewards() {
        return rewards;
    }

    public void setRewards(Integer rewards) {
        this.rewards = rewards;
    }
    // Constructors, getters, and setters


    public static Program fromJson(JSONObject jsonObject) throws JSONException {
        Program program = new Program();

        program.setId(jsonObject.optLong("id"));
        program.setName(jsonObject.optString("name"));
        program.setCategory(jsonObject.optString("category"));
        program.setFrequency(jsonObject.optString("frequency"));
        program.setStartDate(new Date(jsonObject.optLong("startDate")));
        program.setNumberOfPeriods(jsonObject.optInt("numberOfPeriods"));

        JSONObject smallPunishmentJson = jsonObject.optJSONObject("smallPunishment");
        if (smallPunishmentJson != null) {
            program.setSmallPunishment(Punishment.fromJson(smallPunishmentJson));
        }

        JSONObject bigPunishmentJson = jsonObject.optJSONObject("bigPunishment");
        if (bigPunishmentJson != null) {
            program.setBigPunishment(Punishment.fromJson(bigPunishmentJson));
        }

        program.setStatus(Status.valueOf(jsonObject.optString("status")));
//        program.setRewards(jsonObject.optInt("rewards"));

        JSONArray tasksArray = jsonObject.optJSONArray("tasks");
        if (tasksArray != null) {
            List<Task> tasks = new ArrayList<>();
            for (int i = 0; i < tasksArray.length(); i++) {
                tasks.add(Task.fromJson(tasksArray.getJSONObject(i)));
            }
            program.setTasks(tasks);
        }


        // Handling programOccurrences is similar to tasks
        // ...

        JSONArray programOccurrenceArray = jsonObject.optJSONArray("programOccurrences");
        if (programOccurrenceArray != null) {
            List<ProgramOccurrence> programOccurrences = new ArrayList<>();
            for (int i = 0; i < programOccurrenceArray.length(); i++) {
                programOccurrences.add(ProgramOccurrence.fromJson(programOccurrenceArray.getJSONObject(i)));
            }
            program.setProgramOccurrences(programOccurrences);
        }


        return program;
    }
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("name", name);
            jsonObject.put("category", category);
            jsonObject.put("frequency", frequency);
            jsonObject.put("startDate", startDate != null ? startDate.getTime() : null);
            jsonObject.put("numberOfPeriods", numberOfPeriods);

            if (smallPunishment != null) {
                jsonObject.put("smallPunishment", smallPunishment.toJson());
            }

            if (bigPunishment != null) {
                jsonObject.put("bigPunishment", bigPunishment.toJson());
            }

            jsonObject.put("status", status != null ? status.name() : null);
//            jsonObject.put("rewards", rewards);

            JSONArray tasksArray = new JSONArray();
            for (Task task : tasks) {
                tasksArray.put(task.toJson());
            }
            jsonObject.put("tasks", tasksArray);

            // Handle list of programOccurrences
            JSONArray occurrencesArray = new JSONArray();
            for (ProgramOccurrence occurrence : programOccurrences) {
                occurrencesArray.put(occurrence.toJson());
            }
            jsonObject.put("programOccurrences", occurrencesArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}

