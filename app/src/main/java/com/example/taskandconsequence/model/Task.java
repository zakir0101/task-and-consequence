package com.example.taskandconsequence.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Task {
    private Long id;
    private String name ;
    private String description;
    private int Rewards ;
    private Long programId; // Reference to the Program

    // Constructors, getters, and setters


    public Task() {
    }

    public Task(String name, String description, int rewards, Long programId) {
        this.name = name;
        this.description = description;
        Rewards = rewards;
        this.programId = programId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getProgramId() {
        return programId;
    }

    public void setProgramId(Long programId) {
        this.programId = programId;
    }

    public int getRewards() {
        return Rewards;
    }

    public void setRewards(int rewards) {
        Rewards = rewards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Task fromJson(JSONObject jsonObject) throws JSONException {
        Task task = new Task();
        task.setId(jsonObject.optLong("id"));
        task.setName(jsonObject.optString("name"));
        task.setDescription(jsonObject.optString("description"));
        task.setRewards(jsonObject.optInt("rewards"));
        task.setProgramId(jsonObject.optLong("programId"));
        return task;
    }


    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("name", name);
            jsonObject.put("description", description);
            jsonObject.put("rewards", Rewards);
            jsonObject.put("programId", programId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}

