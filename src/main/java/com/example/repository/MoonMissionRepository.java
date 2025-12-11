package com.example.repository;

import com.example.model.MoonMission;

import java.util.List;
import java.util.Optional;

/**
 * Interface defining the operations for managing MoonMissions in the repository.
 */
public interface MoonMissionRepository {
    List<MoonMission> listMissions();
    Optional<MoonMission> getMissionById(int missionId);
    int countMissionsByYear(int year);
}