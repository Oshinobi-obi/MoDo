package com.application.modo;
public class ProfileBadgesItem {
    private String statusTitle;
    private String statusDescription;
    private String statusRequirements;

    public ProfileBadgesItem(String statusTitle, String statusDescription, String statusRequirements) {
        this.statusTitle = statusTitle;
        this.statusDescription = statusDescription;
        this.statusRequirements = statusRequirements;
    }

    public String getStatusTitle() {
        return statusTitle;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public String getStatusRequirements() {
        return statusRequirements;
    }
}
