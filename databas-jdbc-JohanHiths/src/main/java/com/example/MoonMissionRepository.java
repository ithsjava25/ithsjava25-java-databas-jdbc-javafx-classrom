package com.example;


import java.util.List;

public interface MoonMissionRepository {
    List<MoonMission> listAllMissions();
    MoonMission findMoonMissionById(int missionId);
    int countMissionsByYear(int year);
}
