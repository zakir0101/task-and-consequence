package com.example.taskandconsequence.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProgramOccurrence {
    private Long id;
    private Long programId;
    private Date date;
    private Status status;
    private Status punishmentStatus; // Indicates if the punishment is completed

    private List<TaskOccurrence> taskOccurrences;
    // Constructors, getters, and setters
    private Integer rewards;
    public ProgramOccurrence() {
    }

    public ProgramOccurrence(Long id, Long programId, Date date, Status status, Status punishmentStatus) {
        this.id = id;
        this.programId = programId;
        this.date = date;
        this.status = status;
        this.punishmentStatus = punishmentStatus;
    }

    public List<TaskOccurrence> getTaskOccurrences() {
        return taskOccurrences;
    }

    public void setTaskOccurrences(List<TaskOccurrence> taskOccurrences) {
        this.taskOccurrences = taskOccurrences;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProgramId() {
        return programId;
    }

    public void setProgramId(Long programId) {
        this.programId = programId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getPunishmentStatus() {
        return punishmentStatus;
    }

    public void setPunishmentStatus(Status punishmentStatus) {
        this.punishmentStatus = punishmentStatus;
    }

    public Integer getRewards() {
        return rewards;
    }

    public void setRewards(Integer rewards) {
        this.rewards = rewards;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("programId", programId);
            jsonObject.put("date", date != null ? date.getTime() : null);
            jsonObject.put("status", status != null ? status.name() : null);
            jsonObject.put("punishmentStatus", punishmentStatus != null ? punishmentStatus.name() : null);
//            jsonObject.put("rewards", rewards);

            JSONArray taskOccurrencesArray = new JSONArray();
            if (taskOccurrences != null) {
                for (TaskOccurrence taskOccurrence : taskOccurrences) {
                    taskOccurrencesArray.put(taskOccurrence.toJson());
                }
            }
            jsonObject.put("taskOccurrences", taskOccurrencesArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    public static ProgramOccurrence fromJson(JSONObject jsonObject) throws JSONException {
        ProgramOccurrence programOccurrence = new ProgramOccurrence();
        programOccurrence.setId(jsonObject.optLong("id"));
        programOccurrence.setProgramId(jsonObject.optLong("programId"));
        programOccurrence.setDate(new Date(jsonObject.optLong("date")));
        programOccurrence.setStatus(Status.valueOf(jsonObject.optString("status")));
        programOccurrence.setPunishmentStatus(Status.valueOf(jsonObject.optString("punishmentStatus")));
//        programOccurrence.setRewards(jsonObject.optInt("rewards"));

        List<TaskOccurrence> taskOccurrenceList = new ArrayList<>();
        JSONArray taskOccurrencesArray = jsonObject.optJSONArray("taskOccurrences");
        if (taskOccurrencesArray != null) {
            for (int i = 0; i < taskOccurrencesArray.length(); i++) {
                JSONObject taskOccurrenceJson = taskOccurrencesArray.getJSONObject(i);
                TaskOccurrence taskOccurrence = TaskOccurrence.fromJson(taskOccurrenceJson);
                taskOccurrenceList.add(taskOccurrence);
            }
        }
        programOccurrence.setTaskOccurrences(taskOccurrenceList);

        return programOccurrence;
    }

}