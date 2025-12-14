package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Om vi kör i DevMode: Starta testdatabasen, via Docker/Testcontainers.
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        // Startar programlogiken.
        new Main().run();
    }

    public void run() {
        // Hämtar databasinställningar.
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        // Om någon databasinställning skulle saknas, så avbryts programmet.
        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS as system properties (-Dkey=value) or environment variables."
            );
        }

        // Skapar anslutning till databasen.
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
            // För att läsa input av användaren i terminalen, så används Scanner.
            Scanner scanner = new Scanner(System.in);

            // Loggar in med användarnamn och lösenord.
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            // En SQL-fråga för att kontrollera användarnamn och lösenord.
            String loginSql = "SELECT * FROM account WHERE name = ? AND password = ?";
            try (PreparedStatement stmt = connection.prepareStatement(loginSql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                try (ResultSet rs = stmt.executeQuery()) {
                    // Om ingen rad hittas, så misslyckas inloggningen.
                    if (!rs.next()) {
                        System.out.println("Invalid username or password");
                        return; // Avslutar run() om inloggningen misslyckas.
                    }
                }
            }
            // Fortsätter programmet efter lyckad inloggning.
            System.out.println("Login successful!");

            // En meny-loop.
            boolean running = true;
            while (running) {
                System.out.println("\nMenu:");
                System.out.println("1) List moon missions");
                System.out.println("2) Get moon mission by ID");
                System.out.println("3) Count missions for a year");
                System.out.println("4) Create account");
                System.out.println("5) Update account password");
                System.out.println("6) Delete account");
                System.out.println("0) Exit");
                System.out.print("Choose an option: ");

                // Läser in användarens menyval.
                switch (scanner.nextLine()) {
                    case "1" -> listMissions(connection);
                    case "2" -> getMissionById(connection, scanner);
                    case "3" -> countMissionsByYear(connection, scanner);
                    case "4" -> createAccount(connection, scanner);
                    case "5" -> updatePassword(connection, scanner);
                    case "6" -> deleteAccount(connection, scanner);
                    case "0" -> {
                        running = false;
                        System.out.println("Exiting program...");
                    }
                    default -> System.out.println("Invalid option.");
                }
            }

        } catch (SQLException e) {
            // Om något skulle gå fel med databasen.
            throw new RuntimeException(e);
        }
    }

    // Listar alla rymduppdrag.
    private void listMissions(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT spacecraft FROM moon_mission");
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("Moon missions:");
            while (rs.next()) {
                System.out.println(rs.getString("spacecraft"));
            }
        }
    }

    // Hämtar ett specifikt uppdrag baserat på mission_id.
    private void getMissionById(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter mission_id: ");
        String id = scanner.nextLine();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT spacecraft, launch_date FROM moon_mission WHERE mission_id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    // Hämtar årtal från launch_date.
                    int year = rs.getDate("launch_date").toLocalDate().getYear();
                    System.out.println("Mission: " + rs.getString("spacecraft") + ", Year: " + year);
                } else {
                    System.out.println("Mission not found.");
                }
            }
        }
    }

    // Räknar hur många uppdrag som skedde ett specifikt år.
    private void countMissionsByYear(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter year: ");
        String year = scanner.nextLine();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) AS count FROM moon_mission WHERE YEAR(launch_date) = ?")) {
            stmt.setString(1, year);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Year: " + year + ", Number of missions: " + rs.getInt("count"));
                }
            }
        }
    }

    // Skapar ett nytt konto.
    private void createAccount(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("First name: ");
        String firstName = scanner.nextLine();
        System.out.print("Last name: ");
        String lastName = scanner.nextLine();
        System.out.print("SSN: ");
        String ssn = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO account (first_name, last_name, ssn, password) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, ssn);
            stmt.setString(4, password);
            stmt.executeUpdate();
            System.out.println("Account created!");
        }
    }

    // Uppdaterar ett lösenord för ett konto.
    private void updatePassword(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("User ID: ");
        String userId = scanner.nextLine();
        System.out.print("New password: ");
        String newPassword = scanner.nextLine();

        try (PreparedStatement stmt = conn.prepareStatement("UPDATE account SET password = ? WHERE user_id = ?")) {
            stmt.setString(1, newPassword);
            stmt.setString(2, userId);
            int rows = stmt.executeUpdate();
            System.out.println(rows > 0 ? "Password updated!" : "User not found.");
        }
    }

    // Tar bort ett konto.
    private void deleteAccount(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("User ID: ");
        String userId = scanner.nextLine();

        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM account WHERE user_id = ?")) {
            stmt.setString(1, userId);
            int rows = stmt.executeUpdate();
            System.out.println(rows > 0 ? "Account deleted!" : "User not found.");
        }
    }

    // Avgör om programmet körs i DevMode.
    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode")) return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE"))) return true;
        return Arrays.asList(args).contains("--dev");
    }

    // Läser värden från system properties eller environment variabler.
    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}