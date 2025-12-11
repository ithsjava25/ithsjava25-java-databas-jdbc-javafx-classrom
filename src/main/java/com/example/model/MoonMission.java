package com.example.model;

import java.sql.Date;

/**
 * Represents a moon mission with its main details.
 *
 * @param missionId     unique identifier for the mission
 * @param spacecraft    name of the spacecraft
 * @param launchDate    launch date of the mission
 * @param carrierRocket rocket used to carry the spacecraft
 * @param operator      organization responsible for the mission
 * @param missionType   type of mission (e.g., manned, unmanned)
 * @param outcome       result or outcome of the mission
 */
public record MoonMission(
        int missionId,
        String spacecraft,
        Date launchDate,
        String carrierRocket,
        String operator,
        String missionType,
        String outcome
) {

    /**
     * Returns a readable string representation of the moon mission.
     */
    @Override
    public String toString() {
        return "MoonMission: " +
                "missionId: " + missionId +
                ", spacecraft: " + spacecraft +
                ", launchDate: " + launchDate +
                ", carrierRocket: " + carrierRocket +
                ", operator: " + operator +
                ", missionType: " + missionType +
                ", outcome: " + outcome;
    }
}
