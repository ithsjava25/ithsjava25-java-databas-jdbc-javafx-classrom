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

    public void run() {
        // Resolve database configuration
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        // Test connection
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Scanner scanner = new Scanner(System.in);

        // Login flow
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (!validateLogin(jdbcUrl, dbUser, dbPass, username, password)) {
            System.out.println("Invalid username or password.");
            System.out.println("0) Exit");
            scanner.nextLine();
            scanner.close();
            return;
        }

        System.out.println("Login successful!");

        // Menu loop
        boolean running = true;
        while (running) {
            System.out.println("\n1) List moon missions");
            System.out.println("2) Get a moon mission by mission_id");
            System.out.println("3) Count missions for a given year");
            System.out.println("4) Create an account");
            System.out.println("5) Update an account password");
            System.out.println("6) Delete an account");
            System.out.println("0) Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    listMoonMissions(jdbcUrl, dbUser, dbPass);
                    break;
                case "2":
                    getMissionById(scanner, jdbcUrl, dbUser, dbPass);
                    break;
                case "3":
                    countMissionsByYear(scanner, jdbcUrl, dbUser, dbPass);
                    break;
                case "4":
                    createAccount(scanner, jdbcUrl, dbUser, dbPass);
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }

        scanner.close();
    }

    // List all moon missions
    private void listMoonMissions(String jdbcUrl, String dbUser, String dbPass) {
        String sql = "SELECT spacecraft FROM moon_mission ORDER BY mission_id";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                System.out.println(rs.getString("spacecraft"));
            }

        } catch (SQLException e) {
            System.err.println("Error listing missions: " + e.getMessage());
        }
    }

    // Get mission by ID
    private void getMissionById(Scanner scanner, String jdbcUrl, String dbUser, String dbPass) {
        System.out.print("Enter mission_id: ");
        String input = scanner.nextLine().trim();

        try {
            long missionId = Long.parseLong(input);
            String sql = "SELECT mission_id, spacecraft, launch_date FROM moon_mission WHERE mission_id = ?";

            try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, missionId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Mission ID: " + rs.getLong("mission_id"));
                        System.out.println("Spacecraft: " + rs.getString("spacecraft"));
                        System.out.println("Launch Date: " + rs.getDate("launch_date"));
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid mission ID format");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Count missions by year
    private void countMissionsByYear(Scanner scanner, String jdbcUrl, String dbUser, String dbPass) {
        System.out.print("Enter year: ");
        String input = scanner.nextLine().trim();

        try {
            int year = Integer.parseInt(input);
            String sql = "SELECT COUNT(*) as count FROM moon_mission WHERE YEAR(launch_date) = ?";

            try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, year);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        System.out.println("Number of missions in " + year + ": " + count);
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid year format");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Create new account
    private void createAccount(Scanner scanner, String jdbcUrl, String dbUser, String dbPass) {
        System.out.print("First name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Last name: ");
        String lastName = scanner.nextLine().trim();

        System.out.print("SSN: ");
        String ssn = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        String sql = "INSERT INTO account (first_name, last_name, ssn, password) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, ssn);
            stmt.setString(4, password);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Account created successfully");
            }

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Validate user credentials
    private boolean validateLogin(String jdbcUrl, String dbUser, String dbPass,
                                  String username, String password) {
        String sql = "SELECT user_id FROM account WHERE name = ? AND password = ?";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            return false;
        }
    }

    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode"))
            return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE")))
            return true;
        return Arrays.asList(args).contains("--dev");
    }

    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}