package com.example.repository;

import com.example.model.MoonMission;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface MoonMissionRepository {
    List<MoonMission> listMissions() throws SQLException;
    Optional<MoonMission> getMissionById(int missionId) throws SQLException;
    int countMissionsByYear(int year) throws SQLException;
}