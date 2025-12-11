package com.example.service;

import com.example.model.MoonMission;
import com.example.repository.MoonMissionRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MoonMissionService {
    private final MoonMissionRepository repo;

    public MoonMissionService(MoonMissionRepository repo) { this.repo = repo; }

    public List<MoonMission> listMissions() throws SQLException { return repo.listMissions(); }

    public Optional<MoonMission> getMissionById(int id) throws SQLException { return repo.getMissionById(id); }

    public int countMissionsByYear(int year) throws SQLException { return repo.countMissionsByYear(year); }
}
