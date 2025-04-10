package com.application.modo;

public class ProfilePointsItem {
    private String title;
    private String description;
    private String points;

    public ProfilePointsItem(String title, String description, String points) {
        this.title = title;
        this.description = description;
        this.points = points;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPoints() {
        return points;
    }
}
