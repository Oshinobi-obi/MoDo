package com.application.modo;
public class CalendarTask {
    private String title;
    private String description;
    private String deadline;

    public CalendarTask(String title, String description, String deadline) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDeadline() { return deadline; }
}