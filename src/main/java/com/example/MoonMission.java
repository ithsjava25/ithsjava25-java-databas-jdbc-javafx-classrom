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

    // SETTERS


    public void setMissionId(int missionId) {
        this.missionId = missionId;
    }

    public void setSpacecraft(String spacecraft) {
        this.spacecraft = spacecraft;
    }

    public void setLaunchDate(LocalDate launchDate) {
        this.launchDate = launchDate;
    }

    public void setCarrierRocket(String carrierRocket) {
        this.carrierRocket = carrierRocket;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public void setMissionType(String missionType) {
        this.missionType = missionType;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}