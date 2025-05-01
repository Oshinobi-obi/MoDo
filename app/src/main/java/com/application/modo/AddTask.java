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

    // 🆕 Default empty constructor (important for Firestore)
    public AddTask() {}

    // 🆕 Full constructor (optional, when you create a new task)
    public AddTask(String title, String description, String priority, String label, String deadline) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.label = label;
        this.deadline = deadline;
        this.status = "Upcoming";
        this.timestamp = Timestamp.now();
    }

    // 🆕 Setters (important for Firestore to set values)

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    // 🆕 Getters

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public String getLabel() {
        return label;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}