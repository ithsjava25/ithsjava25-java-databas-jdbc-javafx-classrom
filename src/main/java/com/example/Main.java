package com.example;

import java.util.Scanner;
import java.util.List;
import java.sql.*;
import java.util.Arrays;

public class Main {
    public void runApplicationMenu(Connection connection) throws SQLException {

        Scanner scanner = new Scanner(System.in);
        MoonMissionRepository missionRepo = new MoonMissionRepositoryJdbc(connection);
        AccountRepository accountRepo = new JdbcAccountRepository(connection);
        boolean isRunning = true;
        while (isRunning) {
            System.out.println("1) List moon missions (prints spacecraft names from `moon_mission`).\n" +
                    "   2) Get a moon mission by mission_id (prints details for that mission).\n" +
                    "   3) Count missions for a given year (prompts: year; prints the number of missions launched that year).\n" +
                    "   4) Create an account (prompts: first name, last name, ssn, password; prints confirmation).\n" +
                    "   5) Update an account password (prompts: user_id, new password; prints confirmation).\n" +
                    "   6) Delete an account (prompts: user_id; prints confirmation).\n" +
                    "   0) Exit.");
            int choice;

            try {
                System.out.println("Enter choice");
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice.");
                continue;
            }
            switch (choice) {
                case 0:
                    isRunning = false;
                    break;
                case 1:
                    List<MoonMission> missions = missionRepo.listAllMissions();
                    System.out.println("\nMoon Missions");
                    for (MoonMission m : missions) {
                        System.out.println(m.getSpacecraft());
                    }

                    break;
                case 2:
                    System.out.print("Enter the mission ID: ");
                    String input = scanner.nextLine();
                    int missionId = Integer.parseInt(input);

                    MoonMission mission = missionRepo.findMoonMissionById(missionId);

                    if (mission == null) {
                        System.out.println(" Mission not found.");
                    } else {
                        System.out.println("\n--- Mission Details ---");
                        System.out.println("ID: " + mission.getMissionId());
                        System.out.println("Spacecraft: " + mission.getSpacecraft());
                        System.out.println("Launch date: " + mission.getLaunchDate());
                        System.out.println("Outcome: " + mission.getOutcome());
                        System.out.println("Carrier rocket: " + mission.getCarrierRocket());
                        System.out.println("------------------------");
                    }
                    break;
                case 3:
                    int year = 0;
                    while (true) {
                        try {
                            System.out.println("Enter the launch year");
                            String yearInput = scanner.nextLine();
                            year = Integer.parseInt(yearInput);
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid year. Please enter a numeric value.");
                        }
                    }

                    int count = missionRepo.countMissionsByYear(year);

                    System.out.println(count);
                    break;


                case 4:
                    System.out.println("Enter first name");
                    String firstName = scanner.nextLine();
                    System.out.println("Enter last name");
                    String lastName = scanner.nextLine();
                    System.out.println("Enter SSN");
                    String ssn = scanner.nextLine();
                    System.out.println("Enter password");
                    String rawPassword = scanner.nextLine();



                    boolean accountCreated = accountRepo.createAccount(firstName, lastName, ssn, rawPassword);

                    if (accountCreated) {
                        System.out.println("Account created successfully.");
                    } else {
                        System.out.println("Account creation failed.");
                    }

                    break;
                case 5:
                    while (true) {
                        System.out.println("Enter the usedId to update password");
                        String idInput = scanner.nextLine();

                        int userId;
                        try {
                            userId = Integer.parseInt(idInput);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid user ID.");
                            break;
                        }

                        System.out.println("Enter new password");
                        String newPassword = IO.readln();
                        if (newPassword.equals("0")) {
                            break;
                        }



                        boolean updatePassword = accountRepo.updatePassword(userId, newPassword);

                        if (updatePassword) {
                            System.out.println("updated");

                        } else {
                            System.out.println("Password update failed.");
                            break;
                        }
                    }
                    break;
                case 6:
                    System.out.println("Enter user ID to delete!");
                    String deleteInput = scanner.nextLine();

                    int deleteId;
                    try {
                        deleteId = Integer.parseInt(deleteInput);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid user ID.");
                        break;
                    }
                    boolean deleted = accountRepo.deleteAccount(deleteId);
                    if (deleted) {
                        System.out.println("deleted");
                    } else {
                        System.out.println("Account delete failed.");
                    }

                    break;

            }

        }

    }


    public static void main(String[] args) throws SQLException {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();


    }

    public void run() throws SQLException {


        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {

            Scanner scanner = new Scanner(System.in);
            AccountRepository accountRepo = new JdbcAccountRepository(connection);

            while (true) {
                System.out.print("Username: ");
                String username = scanner.nextLine().trim();

                System.out.print("Password: ");
                String password = scanner.nextLine().trim();

                if (accountRepo.verifyPassword(username, password)) {
                    System.out.println("Logged in!");
                    runApplicationMenu(connection);
                    return;
                } else {
                    System.out.println("Invalid username or password");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Initial database connection failed.", e);
        }
    }



    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode"))  //Add VM option -DdevMode=true
            return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE")))  //Environment variable DEV_MODE=true
            return true;
        return Arrays.asList(args).contains("--dev"); //Argument --dev
    }


    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }

}
