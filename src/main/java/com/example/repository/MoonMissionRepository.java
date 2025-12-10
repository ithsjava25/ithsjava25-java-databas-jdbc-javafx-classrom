package com.example.repository;

import com.example.model.MoonMission;

import java.util.List;
import java.util.Optional;

public interface MoonMissionRepository {

    List<String> findAllSpaceCraftNames ();

    Optional<MoonMission> findById(int missionId);

    int countByYear(int year);
}
