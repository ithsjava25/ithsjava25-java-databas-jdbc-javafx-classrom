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

            // LOGIN LOOP
            boolean loggedIn = false;
            while (!loggedIn) {
                System.out.print("Username: ");
                String username = readLine(in);

                System.out.print("Password: ");
                String password = readLine(in);

                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT 1 FROM account WHERE name = ? AND password = ?")) {

                    ps.setString(1, username);
                    ps.setString(2, password);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            loggedIn = true;
                            System.out.println("Login successful!");
                        } else {
                            System.out.println("Invalid username or password");
                            System.out.print("Press 0 to exit or enter to retry: ");
                            String exit = readLine(in);
                            if ("0".equals(exit)) return;
                        }
                    }
                }
            }

            boolean exit = false;
            while (!exit) {
                System.out.println("\nMenu:");
                System.out.println("1) List moon missions");
                System.out.println("2) Get a moon mission by mission_id");
                System.out.println("3) Count missions for a given year");
                System.out.println("0) Exit");
                System.out.print("Choose an option: ");
                String choice = readLine(in);

                switch (choice) {
                    case "1":
                        listMoonMissions(connection);
                        break;
                    case "2":
                        getMoonMissionById(connection, in);
                        break;
                    case "3":
                        countMissionsByYear(connection, in);
                        break;
                    case "0":
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid option");
                }
            }

        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    // LIST MOON MISSIONS
    private void listMoonMissions(Connection connection) throws SQLException {
        String sql = "SELECT spacecraft FROM moon_mission";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("Moon Missions:");
            while (rs.next()) {
                System.out.println(rs.getString("spacecraft"));
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




    // Reads a line from System.in using Java 25 IO
    private static String readLine(InputStream in) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') break;
            buffer[len++] = (byte) b;
        }
        return new String(buffer, 0, len).trim();
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