package com.example.repo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MoonMissionRepository {
    List<String> listSpacecraftNames();
    Optional<Map<String, Object>> getMissionAsMap(long missionId);
    int countByYear(int year);
}
