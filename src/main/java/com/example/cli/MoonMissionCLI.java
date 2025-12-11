package com.example.cli;

import com.example.model.MoonMission;
import com.example.service.MoonMissionService;
import com.example.repository.RepositoryException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * CLI handler for moon mission operations.
 * Provides methods for listing, fetching by ID, and counting missions by year.
 */
public class MoonMissionCLI implements ExitMenuHandler {

    private final MoonMissionService service;
    private final InputReader input;

    /**
     * Creates a MoonMissionCLI with the provided service and input reader.
     *
     * @param service service handling moon mission data
     * @param input input reader for user interaction
     */
    public MoonMissionCLI(MoonMissionService service, InputReader input) {
        this.service = service;
        this.input = input;
    }

    /** Lists all moon missions by spacecraft name. */
    public void listMissions() {
        try {
            List<MoonMission> missions = service.listMissions();
            System.out.println("\n-- All Moon Missions --");
            missions.forEach(m -> System.out.println(m.spacecraft()));
            System.out.println("----------------------\n");
        } catch (RepositoryException e) {
            System.out.println("❌ Error listing missions: " + e.getMessage());
        }
    }

    /** Fetches and displays a mission by its ID. */
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
        } catch (RepositoryException e) {
            System.out.println("❌ Error fetching mission: " + e.getMessage());
        }
    }

    /**
     * Counts missions for a given year, prints total and detailed summaries
     * of missions sorted by launch date (most recent first).
     */
    public void countMissionsByYear() {
        var yearWrapper = input.readInt("Year");
        if (handleExitOrMenu(yearWrapper.result())) return;

        int year = yearWrapper.value();

        try {
            int count = service.countMissionsByYear(year);

            System.out.println("\n---- Missions for year " + year + " ----");
            System.out.println("Total missions: " + count);

            List<MoonMission> missions = service.listMissions();
            missions.stream()
                    .filter(m -> m.launchDate().toLocalDate().getYear() == year)
                    .sorted(Comparator.comparing(MoonMission::launchDate).reversed())
                    .forEach(this::printMissionSummary);

            System.out.println("----------------------------------------\n");

        } catch (RepositoryException e) {
            System.out.println("❌ Error counting/listing missions: " + e.getMessage());
        }
    }

    /** Prints a short summary for a single moon mission. */
    private void printMissionSummary(MoonMission m) {
        System.out.printf("Spacecraft: %s | Launch Date: %s | Rocket: %s | Operator: %s%n",
                m.spacecraft(), m.launchDate(), m.carrierRocket(), m.operator());
    }
}