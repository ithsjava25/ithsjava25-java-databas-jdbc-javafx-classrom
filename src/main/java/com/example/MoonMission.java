package com.example;

import java.sql.Date;

public class MoonMission {
    private int mission_id;
    private String spacecraft, carrier_rocket, operator, mission_type, outcome;
    private Date launch_date;

    public MoonMission(int mission_id, String spacecraft, Date launch_date, String carrier_rocket, String operator, String mission_type, String outcome) {
        this.mission_id = mission_id;
        this.spacecraft = spacecraft;
        this.carrier_rocket = carrier_rocket;
        this.operator = operator;
        this.mission_type = mission_type;
        this.outcome = outcome;
        this.launch_date = launch_date;
    }

    public int getMission_id() {
        return mission_id;
    }

    public void setMission_id(int mission_id) {
        this.mission_id = mission_id;
    }

    public String getSpacecraft() {
        return spacecraft;
    }

    public void setSpacecraft(String spacecraft) {
        this.spacecraft = spacecraft;
    }

    public String getCarrier_rocket() {
        return carrier_rocket;
    }

    public void setCarrier_rocket(String carrier_rocket) {
        this.carrier_rocket = carrier_rocket;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getMission_type() {
        return mission_type;
    }

    public void setMission_type(String mission_type) {
        this.mission_type = mission_type;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public Date getLaunch_date() {
        return launch_date;
    }

    public void setLaunch_date(Date launch_date) {
        this.launch_date = launch_date;
    }

    @Override
    public String toString() {
        return "MoonMission{" +
                "mission_id='" + mission_id + '\'' +
                ", spacecraft='" + spacecraft + '\'' +
                ", carrier_rocket='" + carrier_rocket + '\'' +
                ", operator='" + operator + '\'' +
                ", mission_type='" + mission_type + '\'' +
                ", outcome='" + outcome + '\'' +
                ", launch_date=" + launch_date +
                '}';
    }
}
