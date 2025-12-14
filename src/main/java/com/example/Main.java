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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //Todo: Starting point for your code
        Scanner scanner = new Scanner(System.in);

        System.out.println("Username:");
        String username = scanner.nextLine();

        System.out.println("Password:");
        String password = scanner.nextLine();

        boolean isValid = validateLogin(username, password, jdbcUrl, dbUser, dbPass);

        if (!isValid) {
            System.out.println("Invalid username or password");
            // Testarna förväntar sig att man kan välja 0 för att avsluta
            System.out.print("Press 0 to exit or any key to continue...");
            String choice = scanner.nextLine();
            if ("0".equals(choice)) {
                return;
            }
        } else {
            System.out.println("Login successful!");
        }

        // Huvudmeny
        boolean running = true;
        while (running) {
            System.out.println("1) List moon missions");
            System.out.println("2) Get a moon mission by mission_id");
            System.out.println("3) Count missions for a given year");
            System.out.println("4) Create an account");
            System.out.println("5) Update an account password");
            System.out.println("6) Delete an account");
            System.out.println("0) Exit");

            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    listMoonMissions(jdbcUrl, dbUser, dbPass);
                    break;
                case "2":
                    System.out.print("Enter mission ID: ");
                    String missionId = scanner.nextLine();
                    getMissionById(jdbcUrl, dbUser, dbPass, missionId);
                    break;
                case "3":
                    System.out.print("Enter year: ");
                    String year = scanner.nextLine();
                    countMissionsByYear(jdbcUrl, dbUser, dbPass, year);
                    break;
                case "4":
                    createAccount(jdbcUrl, dbUser, dbPass, scanner);
                    break;
                case "5":
                    updateAccountPassword(jdbcUrl, dbUser, dbPass, scanner);
                    break;
                case "6":
                    deleteAccount(jdbcUrl, dbUser, dbPass, scanner);
                    break;
                case "0":
                    System.out.println("Exiting...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice");
            }

            if (running && !choice.equals("0")) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    private boolean validateLogin(String username, String password,
                                  String jdbcUrl, String dbUser, String dbPass) {
        String sql = "SELECT 1 FROM account WHERE name = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
            return false;
        }
    }

    private void listMoonMissions(String jdbcUrl, String dbUser, String dbPass) {
        String sql = "SELECT spacecraft FROM moon_mission ORDER BY mission_id";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=== Moon Missions ===");
            while (rs.next()) {
                System.out.println(rs.getString("spacecraft"));
            }

        } catch (SQLException e) {
            System.err.println("Error listing missions: " + e.getMessage());
        }
    }

    private void getMissionById(String jdbcUrl, String dbUser, String dbPass, String missionId) {
        String sql = "SELECT * FROM moon_mission WHERE mission_id = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(missionId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n=== Mission Details ===");
                System.out.println("Mission ID: " + rs.getInt("mission_id"));
                System.out.println("Spacecraft: " + rs.getString("spacecraft"));
                System.out.println("Launch Date: " + rs.getDate("launch_date"));
                System.out.println("Carrier Rocket: " + rs.getString("carrier_rocket"));
                System.out.println("Operator: " + rs.getString("operator"));
                System.out.println("Mission Type: " + rs.getString("mission_type"));
                System.out.println("Outcome: " + rs.getString("outcome"));
            } else {
                System.out.println("Mission not found");
            }

        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void countMissionsByYear(String jdbcUrl, String dbUser, String dbPass, String year) {
        String sql = "SELECT COUNT(*) as count FROM moon_mission WHERE YEAR(launch_date) = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(year));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("Number of missions in " + year + ": " + count);
            }

        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void createAccount(String jdbcUrl, String dbUser, String dbPass, Scanner scanner) {
        System.out.print("First name: ");
        String firstName = scanner.nextLine();

        System.out.print("Last name: ");
        String lastName = scanner.nextLine();

        System.out.print("SSN: ");
        String ssn = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        // Skapa username från förnamn + första 3 bokstäver av efternamn
        String username = (firstName.substring(0, Math.min(3, firstName.length())) +
                lastName.substring(0, Math.min(3, lastName.length()))).toLowerCase();

        String sql = "INSERT INTO account (first_name, last_name, ssn, name, password) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, ssn);
            stmt.setString(4, username);
            stmt.setString(5, password);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Account created successfully!");
                System.out.println("Username: " + username);
            }

        } catch (SQLException e) {
            System.err.println("Error creating account: " + e.getMessage());
        }
    }

    private void updateAccountPassword(String jdbcUrl, String dbUser, String dbPass, Scanner scanner) {
        System.out.print("Enter user ID: ");
        String userId = scanner.nextLine();

        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();

        String sql = "UPDATE account SET password = ? WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setInt(2, Integer.parseInt(userId));

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Password updated successfully");
            } else {
                System.out.println("No account found with ID: " + userId);
            }

        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void deleteAccount(String jdbcUrl, String dbUser, String dbPass, Scanner scanner) {
        System.out.print("Enter user ID to delete: ");
        String userId = scanner.nextLine();

        String sql = "DELETE FROM account WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(userId));

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Account deleted successfully");
            } else {
                System.out.println("No account found with ID: " + userId);
            }

        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error: " + e.getMessage());
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
}