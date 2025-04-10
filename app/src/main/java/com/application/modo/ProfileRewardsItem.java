package com.application.modo;

public class ProfileRewardsItem {
    private String name;
    private String description;
    private String cost;

    public ProfileRewardsItem(String name, String description, String cost) {
        this.name = name;
        this.description = description;
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCost() {
        return cost;
    }
}
