package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() {
        // Resolve DB settings with precedence: System properties -> Environment variables
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        /*
        System.out.println("Dev Mode Check");
        System.out.println("JDBC URL: " + jdbcUrl);
        System.out.println("DB User: " + dbUser);
        System.out.println("DB Password: " + dbPass);
        System.out.println("======================");
         */

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        InputStream in = System.in;

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {


    // LIST MOON MISSIONS
    private void listMoonMissions(Connection connection) throws SQLException {
        String sql = "SELECT mission_id, spacecraft FROM moon_mission";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("Moon Missions:");
            while (rs.next()) {
                System.out.println(rs.getInt("mission_id") + ": " + rs.getString("spacecraft"));
            }
        }
    }


    // GET MOON MISSION BY ID
    private void getMoonMissionById(Connection connection, InputStream in) throws SQLException, IOException {
        System.out.print("Enter mission_id: ");
        int id = Integer.parseInt(readLine(in));

        String sql = "SELECT * FROM moon_mission WHERE mission_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Spacecraft: " + rs.getString("spacecraft"));
                    System.out.println("Launch Date: " + rs.getDate("launch_date"));
                    System.out.println("Carrier Rocket: " + rs.getString("carrier_rocket"));
                    System.out.println("Operator: " + rs.getString("operator"));
                    System.out.println("Mission Type: " + rs.getString("mission_type"));
                    System.out.println("Outcome: " + rs.getString("outcome"));
                } else {
                    System.out.println("No mission found with id " + id);
                }
            }
        }
    }

    // COUNT MISSIONS BY YEAR
    private void countMissionsByYear(Connection connection, InputStream in) throws SQLException, IOException {
        System.out.print("Enter year: ");
        int year = Integer.parseInt(readLine(in));

        String sql = "SELECT COUNT(*) FROM moon_mission WHERE YEAR(launch_date) = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Number of missions in " + year + ": " + rs.getInt(1));
                }
            }
        }
    }

    // CREATE ACCOUNT
    private void createAccount(Connection connection, InputStream in) throws SQLException, IOException {
        System.out.print("First name: ");
        String firstName = readLine(in);

        System.out.print("Last name: ");
        String lastName = readLine(in);

        System.out.print("SSN: ");
        String ssn = readLine(in);

        System.out.print("Password: ");
        String password = readLine(in);

        String username = (firstName.length() >= 3 ? firstName.substring(0, 3) : firstName)
                + (lastName.length() >= 3 ? lastName.substring(0, 3) : lastName);

        String sql = "INSERT INTO account (name, first_name, last_name, ssn, password) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, ssn);
            ps.setString(5, password);
            ps.executeUpdate();
            System.out.println("Account " + username + " created!");
        }
    }

    // UPDATE ACCOUNT PASSWORD
    private void updateAccountPassword(Connection connection, InputStream in) throws SQLException, IOException {
        // List all accounts first
        listAccounts(connection);

        long userId = -1;
        while (true) {
            System.out.print("Enter the User ID to update: ");
            String input = readLine(in);
            try {
                userId = Long.parseLong(input);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric user ID.");
            }
        }

        System.out.print("New password: ");
        String newPassword = readLine(in);

        String sql = "UPDATE account SET password = ? WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setLong(2, userId);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                System.out.println("Account password updated successfully!");
            } else {
                System.out.println("No account found with user_id " + userId);
            }
        }
    }


    // Helper to list accounts
    private void listAccounts(Connection connection) throws SQLException {
        String sql = "SELECT user_id, first_name, last_name FROM account";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("Accounts:");
            while (rs.next()) {
                System.out.printf("%d: %s %s%n", rs.getLong("user_id"), rs.getString("first_name"), rs.getString("last_name"));
            }
        }
    }

    // DELETE ACCOUNT
    private void deleteAccount(Connection connection, InputStream in) throws SQLException, IOException {
        // List all accounts first
        listAccounts(connection);

        long userId = -1;
        while (true) {
            System.out.print("Enter the User ID to delete: ");
            String input = readLine(in);
            try {
                userId = Long.parseLong(input);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric user ID.");
            }
        }

        String sql = "DELETE FROM account WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                System.out.println("Account deleted successfully!");
            } else {
                System.out.println("No account found with user_id " + userId);
            }
        }
    }
}