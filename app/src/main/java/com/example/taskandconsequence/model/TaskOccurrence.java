package com.example.taskandconsequence.model;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class TaskOccurrence {
    private Long id;
    private Long taskId;
    private Date date;
    private Status status;

    private Long programOccurrenceId ;

    // Constructors, getters, and setters


    public TaskOccurrence() {
    }

    public TaskOccurrence(Long id, Long taskId, Date date, Status status, Long programOccurrenceId) {
        this.id = id;
        this.taskId = taskId;
        this.date = date;
        this.status = status;
        this.programOccurrenceId = programOccurrenceId;
    }

    public Long getProgramOccurrenceId() {
        return programOccurrenceId;
    }

    public void setProgramOccurrenceId(Long programOccurrenceId) {
        this.programOccurrenceId = programOccurrenceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
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

    public static TaskOccurrence fromJson(JSONObject jsonObject) {
        TaskOccurrence taskOccurrence = new TaskOccurrence();
        taskOccurrence.setId(jsonObject.optLong("id"));
        taskOccurrence.setTaskId(jsonObject.optLong("taskId"));
        taskOccurrence.setDate(new Date(jsonObject.optLong("date")));
        taskOccurrence.setStatus(Status.valueOf(jsonObject.optString("status")));
        taskOccurrence.setProgramOccurrenceId(jsonObject.optLong("programOccurrenceId"));
        return taskOccurrence;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("taskId", taskId);
            jsonObject.put("date", date != null ? date.getTime() : null);
            jsonObject.put("status", status.toString());
            jsonObject.put("programOccurrenceId", programOccurrenceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}
