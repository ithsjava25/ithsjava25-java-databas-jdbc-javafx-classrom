package com.example;

import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;

public class
Main {

    private Connection connection; // Koppla upp mot databas


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

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        // Connection, sparas i instansvariabel
        try {
            this.connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
            System.out.println("Connected to database successfully.");
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to database", e);
        }

        //Todo: Starting point for your code

        Scanner scanner = new Scanner(System.in);

        // LOGIN LOOP (mandatory before menu)
        while (true) {
            System.out.print("Username (or 0 to exit): ");
            String username = scanner.nextLine();
            if (username.equals("0")) return;

            System.out.print("Password (or 0 to exit): ");
            String password = scanner.nextLine();
            if (password.equals("0")) return;

            if (login(username, password)) {
                break;
            } else {
                System.out.println("invalid");
            }
        }

        // MENU LOOP AFTER LOGIN
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> listMoonMissions();
                case "2" -> getMoonMissionById(scanner);
                case "3" -> countMissionsByYear(scanner);
                case "4" -> createAccount(scanner);
                case "5" -> updatePassword(scanner);
                case "6" -> deleteAccount(scanner);
                case "0" -> running = false;
                default -> System.out.println("invalid");
            }
        }
    }


    // todo: LOGIN uses column `name` created from CONCAT in init.sql
    private boolean login(String username, String password) {
        String sql = "SELECT * FROM account WHERE name = ? AND password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException("Login failed", e);
        }
    }


    // todo: print CLI menu
    private void printMenu() {
        System.out.println("1) List moon missions");
        System.out.println("2) Get moon mission");
        System.out.println("3) Count missions by year");
        System.out.println("4) Create account");
        System.out.println("5) Update account password");
        System.out.println("6) Delete account");
        System.out.println("0) Exit");
    }

    // todo: choice 1 - List moon missions
    private void listMoonMissions() {
        String sql = "SELECT spacecraft FROM moon_mission";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(rs.getString("spacecraft"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Could not list missions", e);
        }
    }

    // todo: choice 2 - Get mission by ID
    private void getMoonMissionById(Scanner scanner) {
        System.out.print("mission_id: ");
        int id = Integer.parseInt(scanner.nextLine());

        String sql = "SELECT * FROM moon_mission WHERE mission_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println(
                        rs.getInt("mission_id") + " " +
                                rs.getString("spacecraft") + " " +
                                rs.getString("launch_date")
                );
            } else {
                System.out.println("invalid");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
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
