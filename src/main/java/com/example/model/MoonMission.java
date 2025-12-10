package com.example.model;

import java.time.LocalDate;

public record MoonMission(
        int missionId,
        String spacecraft,
        LocalDate launchDate,
        String carrierRocket,
        String operator,
        String missionType,
        String outcome
) {
    public MoonMission {
        if (spacecraft == null || spacecraft.isBlank()) {
            throw new IllegalArgumentException("Spacecraft cannot be blank");
        }
        if (launchDate == null) {
            throw new IllegalArgumentException("Launch date cannot be null");
        }
    }
}