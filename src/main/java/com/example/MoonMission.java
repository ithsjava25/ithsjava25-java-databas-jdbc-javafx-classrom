package com.example;

import java.time.LocalDate;

public class MoonMission {
    private int missionId;
    private String spacecraft;
    private LocalDate launchDate;
    private String carrierRocket;
    private String operator;
    private String missionType;
    private String outcome;

    // GETTERS
    public int getMissionId() {
        return missionId;
    }
    public String getSpacecraft() {
        return spacecraft;
    }
    public LocalDate getLaunchDate() {
        return launchDate;
    }
    public String getCarrierRocket() {
        return carrierRocket;
    }
    public String getOperator() {
        return operator;
    }
    public String getMissionType() {
        return missionType;
    }
    public String getOutcome() {
        return outcome;
    }

    // SETTERS (if needed later)
}