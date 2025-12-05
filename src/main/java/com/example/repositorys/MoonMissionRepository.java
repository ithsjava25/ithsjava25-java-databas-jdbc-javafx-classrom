package com.example.repositorys;

import com.example.MoonMission;

import java.util.List;
import java.util.Optional;

public interface MoonMissionRepository {
    List<String> listMissions();
    Optional<MoonMission> getMissionById(int missionId);
    int countMissionsByYear(int year);
}
