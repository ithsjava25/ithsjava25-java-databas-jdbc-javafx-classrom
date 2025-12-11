package com.example.cli;

import com.example.model.MoonMission;
import com.example.service.MoonMissionService;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MoonMissionCLI implements ExitMenuHandler {

    private final MoonMissionService service;
    private final InputReader input;

    public MoonMissionCLI(MoonMissionService service, InputReader input) {
        this.service = service;
        this.input = input;
    }

    public void listMissions() {
        try {
            List<MoonMission> missions = service.listMissions();
            System.out.println("\n-- All Moon Missions --");
            missions.forEach(m -> System.out.println(m.spacecraft()));
            System.out.println("----------------------\n");
        } catch (SQLException e) {
            System.out.println("❌ Error listing missions: " + e.getMessage());
        }
    }

    public void getMissionById() {
        var idWrapper = input.readInt("Mission ID");
        if (handleExitOrMenu(idWrapper.result())) return;

        try {
            Optional<MoonMission> mission = service.getMissionById(idWrapper.value());
            mission.ifPresentOrElse(
                    m -> {
                        System.out.println("\n-- Mission Details --");
                        System.out.println(m);
                        System.out.println("-------------------\n");
                    },
                    () -> System.out.println("❌ No mission with that ID ❌")
            );
        } catch (SQLException e) {
            System.out.println("❌ Error fetching mission: " + e.getMessage());
        }
    }

    public void countMissionsByYear() {
        var yearWrapper = input.readInt("Year");
        if (handleExitOrMenu(yearWrapper.result())) return;

        try {
            List<MoonMission> missions = service.listMissions();
            System.out.println("\nMissions in " + yearWrapper.value() + " (most recent first):");
            missions.stream()
                    .filter(m -> m.launchDate().toLocalDate().getYear() == yearWrapper.value())
                    .sorted(Comparator.comparing(MoonMission::launchDate).reversed())
                    .forEach(this::printMissionSummary);
            System.out.println("-------------------\n");
        } catch (SQLException e) {
            System.out.println("❌ Error counting missions: " + e.getMessage());
        }
    }

    private void printMissionSummary(MoonMission m) {
        System.out.printf("Spacecraft: %s | Launch Date: %s | Rocket: %s | Operator: %s%n",
                m.spacecraft(), m.launchDate(), m.carrierRocket(), m.operator());
    }
}
