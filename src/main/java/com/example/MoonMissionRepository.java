package com.example;

import java.util.List;

public interface MoonMissionRepository {

    // List of all names:
    List<String> findAllSpacecraftNames();

    //Fetch assignment from ID:
    Optional<MoonMission>findById(long missionId);

    //Number of assignments per year:
    int countByYear(int year);


}
