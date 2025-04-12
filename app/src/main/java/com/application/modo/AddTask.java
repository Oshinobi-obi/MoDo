package com.application.modo;

public class AddTask {
    private String title;
    private String description;
    private String priority;
    private String timeSlot;
    private String deadline;

    public AddTask() {}

    public AddTask(String title, String description, String priority, String timeSlot, String deadline) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.timeSlot = timeSlot;
        this.deadline = deadline;
    }
}