package com.application.modo;

public class AddTask {
    private String title;
    private String description;
    private String priority;
    private String label;
    private String deadline;

    // Required for Firebase
    public AddTask() {}

    public AddTask(String title, String description, String priority, String label, String deadline) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.label = label;
        this.deadline = deadline;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public String getTimeSlot() {
        return label;
    }

    public String getDeadline() {
        return deadline;
    }
}