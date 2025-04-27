package com.application.modo;

public class CompletedTask {
    private String title;
    private String description;
    private String completedDate;

    public CompletedTask() {
        // Empty constructor
    }

    public CompletedTask(String title, String description, String completedDate) {
        this.title = title;
        this.description = description;
        this.completedDate = completedDate;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCompletedDate() {
        return completedDate;
    }
}