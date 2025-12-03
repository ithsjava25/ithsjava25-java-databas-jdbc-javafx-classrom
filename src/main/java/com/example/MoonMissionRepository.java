package com.example;

import java.util.List;

public interface MoonMissionRepository {
    List<String> listMoonMissions();
    List<MoonMission> getMoonMissionById(String id);
    int missionsCountByYear(int year);
}
