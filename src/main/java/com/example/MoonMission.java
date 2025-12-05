package com.example;

import java.time.LocalDate;

public record MoonMission(
        long missionId,
        String spacecraft,
       LocalDate launch_date,
        String carrier_rocket,
        String operator,
        String mission_type,
        String outcome
) { }
