package com.example;

import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;


public class Main {

    static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    /**
     * Determines if the application is running in development mode based on system properties,
     * environment variables, or command-line arguments.
     *
     * @param args an array of command-line arguments
     * @return {@code true} if the application is in development mode; {@code false} otherwise
     */
    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode"))  //Add VM option -DdevMode=true
            return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE")))  //Environment variable DEV_MODE=true
            return true;
        return Arrays.asList(args).contains("--dev"); //Argument --dev
    }

    /**
     * Reads configuration with precedence: Java system property first, then environment variable.
     * Returns trimmed value or null if neither source provides a non-empty value.
     */
    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }

    public void run() {
        // Resolve DB settings with precedence: System properties -> Environment variables
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Username: ");
            if (!scanner.hasNextLine()) {
                return; // Hantera EOF i testmiljön
            }
            String username = scanner.nextLine().trim();

            System.out.print("Password: ");
            if (!scanner.hasNextLine()) {
                return; // Hantera EOF i testmiljön
            }
            String password = scanner.nextLine().trim();

            String sql = " select name from account where name = ? and password = ? ";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);

                try (ResultSet rs = stmt.executeQuery()) {

                    if (rs.next()) {

                        boolean running = true;
                        while (running) {
                            System.out.println("""
                                    
                                       Press a number for  next assignment:
                                    
                                       1) List moon missions (prints spacecraft names from `moon_mission`).
                                       2) Get a moon mission by mission_id (prints details for that mission).
                                       3) Count missions for a given year (prompts: year; prints the number of missions launched that year).
                                       4) Create an account (prompts: first name, last name, ssn, password; prints confirmation).
                                       5) Update an account password (prompts: user_id, new password; prints confirmation).
                                       6) Delete an account (prompts: user_id; prints confirmation).
                                       0) Exit.
                                    """);

                            System.out.print("Choose (0-6): ");
                            if (!scanner.hasNextLine()) {
                                running = false;
                                break;
                            }
                            String choiceStr = scanner.nextLine().trim();

                            int choice;
                            try {
                                choice = Integer.parseInt(choiceStr);
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input. Please enter a number (0-6).");
                                continue;
                            }
                            switch (choice) {
                                case 0:
                                    System.out.println("Exiting application. Goodbye!");
                                    running = false;
                                    break;
                                case 1:
                                    System.out.println("Executing: 1) List moon missions...");
                                    listMoonMissions(connection);
                                    break;
                                case 2:
                                    System.out.println("Executing: 2) Get a moon mission by mission_id...");
                                    moonMissionsById(connection, scanner);
                                    break;
                                case 3:
                                    System.out.println("Executing: 3) Count missions for a given year...");
                                    countingMissionsForAGivenYear(connection, scanner);
                                    break;
                                case 4:
                                    System.out.println("Executing: 4) Create an account...");
                                    createAnAccount(connection, scanner);
                                    break;
                                case 5:
                                    System.out.println("Executing: 5) Update an account password...");
                                    updateAccountPassword(connection, scanner);
                                    break;
                                case 6:
                                    System.out.println("Executing: 6) Delete an account...");
                                    // Todo: Prompt for user_id, then SQL DELETE FROM account WHERE user_id = ?
                                    deleteAccount(connection, scanner);
                                    break;
                                default:
                                    System.out.println("Invalid choice. Please enter a number between 0 and 6.");

                            }
                        }
                    } else {
                        System.out.println("Invalid username or password. Exiting");

                    }
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Database operation failed. " + e.getMessage());
        }
        //Todo: Starting point for your code


    }

    private void deleteAccount(Connection connection, Scanner scanner) {
        System.out.println("Enter user id, that you wish to delete: ");
        if (!scanner.hasNextLine()) {
            return;
        }
        String userId = scanner.nextLine();

        String sql = " delete from account where user_id = ? ";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Successfully deleted the account.");
            } else {
                System.out.println("Failed to delete the account.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database operation failed. " + e.getMessage());
        }
    }

    private void updateAccountPassword(Connection connection, Scanner scanner) {
        System.out.println("Enter user_id: ");
        if (!scanner.hasNextLine()) {
            return;
        }
        String userId = scanner.nextLine();
        System.out.println("Enter new password: ");
        if (!scanner.hasNextLine()) {
            return;
        }
        String newPassword = scanner.nextLine();

        String sql = " update account set password = ? where user_id = ? ";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, userId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Account updated successfully.");
            } else {
                System.out.println("Failed to update account.");
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to update password.");
            throw new RuntimeException("Database operation failed. " + e.getMessage());
        }
    }

    private void createAnAccount(Connection connection, Scanner scanner) {
        System.out.println("Creating an account...");
        System.out.print("Enter first name: ");
        String firstName = scanner.nextLine().trim();
        System.out.print("Enter last name: ");
        String lastName = scanner.nextLine().trim();
        System.out.print("Enter ssn: ");
        String ssn = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        String name = firstName.substring(0, 3) + lastName.substring(0, 3);

        String sql = "INSERT INTO account (first_name, last_name, ssn, password, name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, ssn);
            stmt.setString(4, password);
            stmt.setString(5, name);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Successfully created an account for " + firstName + " " + lastName);
            } else
                System.out.println("Failed to create an account.");

        } catch (SQLException e) {
            throw new RuntimeException("Database operation failed. " + e.getMessage());
        }
    }

    private void countingMissionsForAGivenYear(Connection connection, Scanner scanner) {
        System.out.println("Enter year: ");
        if (!scanner.hasNextInt()) {
            scanner.nextLine();
            System.out.println("Invalid year");
            return;
        }
        int year = scanner.nextInt();
        scanner.nextLine();

        String sql = " select count(*) as mission_count from moon_mission where year(launch_date) = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, year);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("mission_count");
                    System.out.println("Mission count for year: " + year);
                    System.out.println("Number of moon missions: " + count);
                } else
                    System.out.println("No moon missions for year: " + year);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void moonMissionsById(Connection connection, Scanner scanner) {
        System.out.println("Enter moon mission id: ");
        if (!scanner.hasNextInt()) {
            return;
        }
        String missionId = scanner.nextLine().trim();

        String sql = "select spacecraft, mission_id, mission_type, launch_date from moon_mission where mission_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            long id;
            try {
                id = Long.parseLong(missionId);
                stmt.setLong(1, id);
            } catch (NumberFormatException e) {
                System.out.println("Invalid moon mission id. Please enter a number");
                return;
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("ID: " + rs.getLong("mission_id"));
                    System.out.println("Spacecraft: " + rs.getString("spacecraft"));
                    System.out.println("Mission type: " + rs.getString("mission_type"));
                    System.out.println("Launch date: " + rs.getString("launch_date"));
                } else {
                    System.out.println("Mission id " + missionId + " not found. Please enter a number");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    private void listMoonMissions(Connection connection) {
        String sql = " select spacecraft from moon_mission order by spacecraft ";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            boolean found = false;
            while (rs.next()) {
                System.out.println(rs.getString("spacecraft"));
                found = true;
            }
            if (!found) {
                System.out.println("No moon missions found.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error executing List Moon Missions: \" + e.getMessage(), e");
        }
    }
}
