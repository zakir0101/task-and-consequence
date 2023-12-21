package com.example.taskandconsequence.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Punishment {
    private Long id;
    private String name;
    private String severityLevel; // e.g., "small", "big"
    private String description;
    private int deadline; // Number of days to complete the punishment

    // Constructors, getters, and setters


    public Punishment() {
    }

    public Punishment(Long id, String name, String severityLevel, String description, int deadline) {
        this.id = id;
        this.name = name;
        this.severityLevel = severityLevel;
        this.description = description;
        this.deadline = deadline;
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

    public String getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(String severityLevel) {
        this.severityLevel = severityLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Punishment fromJson(JSONObject jsonObject) throws JSONException {
        Punishment punishment = new Punishment();
        punishment.setId(jsonObject.optLong("id"));
        punishment.setName(jsonObject.optString("name"));
        punishment.setSeverityLevel(jsonObject.optString("severityLevel"));
        punishment.setDescription(jsonObject.optString("description"));
        punishment.setDeadline(jsonObject.optInt("deadline"));
        return punishment;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("name", name);
            jsonObject.put("severityLevel", severityLevel);
            jsonObject.put("description", description);
            jsonObject.put("deadline", deadline);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


}
