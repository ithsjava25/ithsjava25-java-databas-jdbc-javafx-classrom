package com.example;

import java.sql.*;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    private final Scanner scanner = new Scanner(System.in);
    private Connection connection;

    public static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() {

        String JDBC_URL = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String DB_USER = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String DB_PASS = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (JDBC_URL == null || DB_USER == null || DB_PASS == null) {
            System.err.println("Database configuration environment variables not set.");
            return;
        }

        try {
            connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
            System.out.println("Database connection established.");

            if (login()) {
                mainMenu();
            } else {
                handleInvalidLogin();
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    // --- Inloggning ---

    private boolean login() throws SQLException {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        String generatedUsername = username.substring(0, Math.min(username.length(), 3))
                + (username.length() > 3 ? username.substring(3, Math.min(username.length(), 6)) : "");

        String sql = "SELECT user_id FROM account WHERE name = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // True om användaren hittades
            }
        }
    }

    private void handleInvalidLogin() {
        System.out.println("Invalid username or password. Press 0 to exit.");
        while (true) {
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            if ("0".equals(choice)) {
                break;
            }
        }
    }

    // --- Menyhantering  ---

    private void mainMenu() {
        int choice = -1;
        while (choice != 0) {
            printMenu();
            try {
                System.out.print("Choice: ");
                choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        listMoonMissions();
                        break;
                    case 2:
                        getMoonMissionById();
                        break;
                    case 3:
                        countMissionsByYear();
                        break;
                    case 4:
                        createAccount();
                        break;
                    case 5:
                        updateAccountPassword();
                        break;
                    case 6:
                        deleteAccount();
                        break;
                    case 0:
                        System.out.println("Exiting application.");
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            } catch (SQLException e) {
                System.err.println("Database operation failed: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\n--- Menu ---");
        System.out.println("1) List moon missions (spacecraft names)");
        System.out.println("2) Get a moon mission by mission_id (details)");
        System.out.println("3) Count missions for a given year");
        System.out.println("4) Create an account (first name, last name, ssn, password)");
        System.out.println("5) Update an account password (user_id, new password)");
        System.out.println("6) Delete an account (user_id)");
        System.out.println("0) Exit");
        System.out.println("------------");
    }

    // 1) List moon missions
    private void listMoonMissions() throws SQLException {
        String sql = "SELECT spacecraft FROM moon_mission";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("--- Moon Missions ---");
            while (rs.next()) {
                System.out.println(" - " + rs.getString("spacecraft"));
            }
        }
    }

    // 2) Get a moon mission by mission_id
    private void getMoonMissionById() throws SQLException {
        System.out.print("Enter mission_id: ");
        long missionId = scanner.nextLong();
        scanner.nextLine();

        String sql = "SELECT * FROM moon_mission WHERE mission_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, missionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("--- Mission Details (ID: " + missionId + ") ---");
                    System.out.println("Spacecraft: " + rs.getString("spacecraft"));
                    System.out.println("Launch Date: " + rs.getDate("launch_date"));
                    System.out.println("Outcome: " + rs.getString("outcome"));
                } else {
                    System.out.println("Mission with ID " + missionId + " not found.");
                }
            }
        }
    }

    // 3) Count missions for a given year
    private void countMissionsByYear() throws SQLException {
        System.out.print("Enter year: ");
        int year = scanner.nextInt();
        scanner.nextLine();

        String sql = "SELECT COUNT(*) FROM moon_mission WHERE YEAR(launch_date) = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, year);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("Found " + count + " missions launched in " + year + ".");
                }
            }
        }
    }

    // 4) Create an account
    private void createAccount() throws SQLException {
        System.out.print("Enter first name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter last name: ");
        String lastName = scanner.nextLine();
        System.out.print("Enter ssn: ");
        String ssn = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        String username = firstName.substring(0, Math.min(firstName.length(), 3))
                + lastName.substring(0, Math.min(lastName.length(), 3));

        String sql = "INSERT INTO account (first_name, last_name, ssn, password, name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, ssn);
            stmt.setString(4, password);
            stmt.setString(5, username);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Account created successfully. Username is " + username + ".");
            } else {
                System.out.println("Failed to create account.");
            }
        }
    }

    // 5) Update an account password
    private void updateAccountPassword() throws SQLException {
        System.out.print("Enter user_id to update: ");
        long userId = scanner.nextLong();
        scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();

        String sql = "UPDATE account SET password = ? WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setLong(2, userId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Account password updated successfully for user_id " + userId + ".");
            } else {
                System.out.println("No account found with user_id " + userId + " to update.");
            }
        }
    }

    // 6) Delete an account
    private void deleteAccount() throws SQLException {
        System.out.print("Enter user_id to delete: ");
        long userId = scanner.nextLong();
        scanner.nextLine();

        String sql = "DELETE FROM account WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Account deleted successfully for user_id " + userId + ".");
            } else {
                System.out.println("No account found with user_id " + userId + " to delete.");
            }
        }
    }


    private void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }


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

}