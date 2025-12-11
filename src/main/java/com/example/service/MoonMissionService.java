package com.example.service;

import com.example.model.MoonMission;
import com.example.repository.MoonMissionRepository;
import com.example.repository.RepositoryException;

import java.util.List;
import java.util.Optional;

public class MoonMissionService {
    private final MoonMissionRepository repo;

    public MoonMissionService(MoonMissionRepository repo) {
        this.repo = repo;
    }

    public List<MoonMission> listMissions() {
        try {
            return repo.listMissions();
        } catch (Exception e) {
            throw new RepositoryException("Error listing missions", e);
        }
    }

    public Optional<MoonMission> getMissionById(int id) {
        try {
            return repo.getMissionById(id);
        } catch (Exception e) {
            throw new RepositoryException("Error fetching mission by ID", e);
        }
    }

    public int countMissionsByYear(int year) {
        try {
            return repo.countMissionsByYear(year);
        } catch (Exception e) {
            throw new RepositoryException("Error counting missions by year", e);
        }
    }
}