package com.example;



import java.sql.Date;


public class MoonMission {
    private int missionId;
    private String spacecraft;
    private Date launchDate;
    private String carrierRocket;
    private String operator;
    private String missionType;
    private String outcome;

    public MoonMission(int missionId, String spacecraft, Date launchDate,
                       String carrierRocket, String operator, String missionType, String outcome) {
        this.missionId = missionId;
        this.spacecraft = spacecraft;
        this.launchDate = launchDate;
        this.carrierRocket = carrierRocket;
        this.operator = operator;
        this.missionType = missionType;
        this.outcome = outcome;
    }
//

    public String getSpacecraft() {

        return spacecraft;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public String getCarrierRocket() {
        return carrierRocket;
    }

    public int getMissionId() {

        return missionId;
    }

    public String getMissionName() {
        return missionType;
    }

    public String getOutcome() {
        return outcome;
    }

    @Override
    public String toString() {
        return "MoonMission {" +
                "missionId=" + missionId +
                ", spacecraft='" + spacecraft + '\'' +
                ", launchDate=" + launchDate +
                ", carrierRocket='" + carrierRocket + '\'' +
                ", operator='" + operator + '\'' +
                ", missionType='" + missionType + '\'' +
                ", outcome='" + outcome + '\'' +
                '}';
    }


}