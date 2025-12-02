package com.example;

public record MoonMission(
        long missionId,
        String spacecraftName,
        int launchYear,
        String description
) { }
