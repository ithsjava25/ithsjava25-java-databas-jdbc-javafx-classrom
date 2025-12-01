package com.example;

import java.util.Date;

public record MoonMission(int missionId, String spacecraft, Date launchDate, String carrierRocket,
                          String operator, String missionType, String outcome) {}