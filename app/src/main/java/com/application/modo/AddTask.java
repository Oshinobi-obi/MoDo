package com.application.modo;

import com.google.firebase.Timestamp;

public class AddTask {
    private String title;
    private String description;
    private String priority;
    private String label;
    private String deadline;
    private String status;
    private Timestamp timestamp;
    private String duration;
    private Long endTime;          // 🆕 for countdown logic

    public AddTask() {
        // Required for Firestore deserialization
    }

    public AddTask(String title, String description, String priority, String label, String deadline, String duration) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.label = label;
        this.deadline = deadline;
        this.duration = duration;
        this.status = "Upcoming";
        this.timestamp = Timestamp.now();
    }

    // Getters
    public String getTitle() { return title; }
    public String getDuration() { return duration; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public String getLabel() { return label; }
    public String getDeadline() { return deadline; }
    public String getStatus() { return status; }
    public Timestamp getTimestamp() { return timestamp; }
    public Long getEndTime() { return endTime; }         // 🆕

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setDescription(String description) { this.description = description; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setLabel(String label) { this.label = label; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public void setStatus(String status) { this.status = status; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public void setEndTime(Long endTime) { this.endTime = endTime; } // 🆕
}