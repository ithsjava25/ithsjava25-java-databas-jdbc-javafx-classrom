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
        // Resolve DB settings with precedence: System properties -> Environment variables
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
            boolean loggedIn = false;
            while (!loggedIn) {
                System.out.print("Username: ");
                String username = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();

                if (isValidLogin(connection,username,password)) {
                    loggedIn = true;
                    System.out.println("Logged in as " + username);
                } else  {
                    System.out.println("Invalid username or password");
                    System.out.println("0) Exit");
                    System.out.println("Press any other key to try again");

                    String choice = scanner.nextLine();
                    if (choice.equals("0")) {
                        return;
                    }
                }
            }
            while (true) {
                printMenu();
                String option = scanner.nextLine();

                    switch (option) {
                        case "1":
                            listMoonMissions(connection);
                            break;
                        case "2":
                            System.out.print("Enter mission_id: ");
                            String idStr = scanner.nextLine();
                            getMoonMissionById(connection, idStr);
                            break;
                        case "3":
                            System.out.print("Enter year: ");
                            String yearStr = scanner.nextLine();
                            countMissionsByYear(connection, yearStr);
                            break;
                        case "4":
                            createAccount(connection, scanner);
                            break;
                        case "5":
                            System.out.println("Not implemented yet (Step 3)");
                            break;
                        case "6":
                            System.out.println("Not implemented yet (Step 3)");
                            break;
                        case "0":
                            return;
                        default:
                            System.out.println("Invalid option, try again.");
                }

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void printMenu() {
        System.out.println("\n--- Moon Mission Menu ---");
        System.out.println("1. List moon missions");
        System.out.println("2. Get moon mission by id");
        System.out.println("3. Count missions by year");
        System.out.println("4. Create an account");
        System.out.println("5. Update an account password");
        System.out.println("6. Delete an account");
        System.out.println("0. Exit");
        System.out.print("Choice: ");
    }

    private void listMoonMissions(Connection conn) throws SQLException {
        String query = "SELECT * FROM moon_mission";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                System.out.println(rs.getString("name"));
            }
        }
    }

    private void getMoonMissionById(Connection conn, String id) throws SQLException {
        String query = "SELECT * FROM moon_mission WHERE mission_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Mission: " + rs.getString("name") +
                            " (ID: " + rs.getInt("mission_id") +
                            "), Year: " + rs.getInt("launch_year"));
                } else {
                    System.out.println("Mission not found.");
                }
            }
        }
    }

    private void countMissionsByYear(Connection conn, String year) throws SQLException {
        String query = "SELECT COUNT(*) FROM moon_mission WHERE launch_year = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, year);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("Found " + count + " missions in " + year);
                }
            }
        }
    }

    private void createAccount(Connection conn, Scanner sc) throws SQLException {
        System.out.print("First name: ");
        String first = sc.nextLine();
        System.out.print("Last name: ");
        String last = sc.nextLine();
        System.out.print("SSN: ");
        String ssn = sc.nextLine();
        System.out.print("Password: ");
        String pass = sc.nextLine();

        String query = "INSERT INTO account (first_name, last_name, ssn, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, first);
            stmt.setString(2, last);
            stmt.setString(3, ssn);
            stmt.setString(4, pass);
            stmt.executeUpdate();
            System.out.println("Account created.");
        }
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
    private boolean isValidLogin(Connection conn, String user, String pass) throws SQLException {
        String query = "SELECT * FROM account WHERE name = ? AND password = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user);
            stmt.setString(2, pass);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
