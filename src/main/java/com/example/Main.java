package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        //Kollar om applikationen körs i utvecklarläge genom anrop till isDevMode-metoden
        if (isDevMode(args)) {
            //Körs den i utvecklarläge, startar den en testdatabas
            DevDatabaseInitializer.start();
        }
        //Skapar en ny instans av Main och anropar run() metoden för att starta applikationen
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

        //Inloggning
        boolean inloggad = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (!inloggad) {
                try {
                    System.out.print("Username: ");
                    String username = reader.readLine();
                    System.out.print("Password: ");
                    String password = reader.readLine();
                    inloggad = validateLogin(jdbcUrl, dbUser, dbPass, username, password);
                    if (!inloggad) {
                        System.out.println("Invalid username or password. Press 0 to exit.");
                        if (reader.readLine().equals("0")) {
                            return;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }

            //Meny
            boolean running = true;
            while (running) {
                printMenu();
                try {
                    String choice = reader.readLine();
                    switch (choice) {
                        case "1":
                            listMoonMissions(jdbcUrl, dbUser, dbPass);
                            break;
                        case "2":
                            getMoonMissionById(reader, jdbcUrl, dbUser, dbPass);
                            break;
                        case "3":
                            countMissionsByYear(reader, jdbcUrl, dbUser, dbPass);
                            break;
                        case "4":
                            createAccount(reader, jdbcUrl, dbUser, dbPass);
                            break;
                        case "5":
                            updateAccountPassword(reader, jdbcUrl, dbUser, dbPass);
                            break;
                        case "6":
                            deleteAccount(reader, jdbcUrl, dbUser, dbPass);
                            break;
                        case "0":
                            running = false;    //Avsluta applikationen
                            break;
                        default:
                            System.out.println("Invalid option. Try again");
                    }
                } catch (IOException e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize reader: " + e.getMessage());
        }
    }

    private void printMenu() {

        System.out.println(
                """
                ========== MENU ==========
                1) List moon missions
                2) Get a moon mission by ID
                3) Count missions for a given year
                4) Create an account
                5) Update an account password
                6) Delete an account
                0) Exit
                ==========================
                """
        );
        System.out.print("Choose an option: ");
    }

    private void listMoonMissions(String jdbcUrl, String dbUser, String dbPass) {
        String sql = "SELECT spacecraft FROM moon_mission";
        try (
            Connection databaseConnection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
            Statement statement = databaseConnection.createStatement();
            ResultSet spacecraftResult = statement.executeQuery(sql)
        ) {
            System.out.println("\nMoon Missions:");
            while (spacecraftResult.next()) {
                System.out.println("- " + spacecraftResult.getString("spacecraft"));
            }
        } catch (SQLException e) {
            System.err.println("Error listing moon missions: " + e.getMessage());
        }

    }

    private void getMoonMissionById(BufferedReader reader, String jdbcUrl, String dbUser, String dbPass) {
        try {
            System.out.print("Enter mission ID: ");
            long missionId = Long.parseLong(reader.readLine());

            String sql = "SELECT mission_id, spacecraft, launch_date FROM moon_mission WHERE mission_id = ?";
            try (
                    Connection databaseConnection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
                    PreparedStatement missionQuery = databaseConnection.prepareStatement(sql)
            ) {
                missionQuery.setLong(1, missionId);
                ResultSet missionResult = missionQuery.executeQuery();

                if (missionResult.next()) {
                    System.out.println("\nMission Details:");  // Rubrik för tydlighet
                    System.out.println("Mission ID: " + missionResult.getLong("mission_id"));  // Innehåller "id"
                    System.out.println("Spacecraft: " + missionResult.getString("spacecraft"));  // Innehåller "Luna 3"
                    System.out.println("Launch Date: " + missionResult.getDate("launch_date"));  // Extra info
                } else {
                    System.out.println("No mission found with ID: " + missionId);
                }
            }
        } catch (SQLException | IOException | NumberFormatException e) {
            System.err.println("Error: " + e.getMessage());
        }

    }

    private void countMissionsByYear(BufferedReader reader, String jdbcUrl, String dbUser, String dbPass) {
        try {
            System.out.print("Enter year: ");
            int year = Integer.parseInt(reader.readLine());

            String sql = "SELECT COUNT(*) AS count FROM moon_mission WHERE YEAR(launch_date) = ?";
            try (
                    Connection databaseConnection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
                    PreparedStatement countQuery = databaseConnection.prepareStatement(sql)
            ) {
                countQuery.setInt(1, year);
                ResultSet countResult = countQuery.executeQuery();

                if (countResult.next()) {
                    int missionCount = countResult.getInt("count");
                    System.out.printf(
                            "\nNumber of missions in %d: %d\n",  // Innehåller året och antalet
                            year,                                  // 2019
                            missionCount                                  // 3
                    );
                }
            }
        } catch (SQLException | IOException | NumberFormatException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void createAccount(BufferedReader reader, String jdbcUrl, String dbUser, String dbPass) {
        try {
            System.out.print("Enter first name: ");
            String firstName = reader.readLine().trim();
            System.out.print("Enter last name: ");
            String lastName = reader.readLine().trim();
            System.out.print("Enter SSN: ");
            String ssn = reader.readLine();
            System.out.print("Enter password: ");
            String password = reader.readLine();

            //Validera att namn inte är tomma
            if (firstName.isEmpty() || lastName.isEmpty()) {
                System.out.println("Error: First name and last name cannot be empty.");
                return;
            }

            //Generera användarnamn på ett säkert sätt
            String firstPart = firstName.length() >= 3 ?
                    firstName.substring(0, 3).toLowerCase() :
                    firstName.toLowerCase();

            String lastPart = lastName.length() >= 3 ?
                    lastName.substring(0, 3).toLowerCase() :
                    lastName.toLowerCase();

            String name = firstPart + lastPart;

            String sql = "INSERT INTO account (first_name, last_name, ssn, password, name) VALUES (?, ?, ?, ?, ?)";
            try (
                    Connection databaseConnection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
                    PreparedStatement insertAccount = databaseConnection.prepareStatement(sql)
            ) {
                insertAccount.setString(1, firstName);
                insertAccount.setString(2, lastName);
                insertAccount.setString(3, ssn);
                insertAccount.setString(4, password);
                insertAccount.setString(5, name);

                int rowsAffected = insertAccount.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Account created!");  // Bekräftelse som testet letar efter
                } else {
                    System.out.println("Failed to create account.");
                }
            }
        } catch (SQLException | IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void updateAccountPassword(BufferedReader reader, String jdbcUrl, String dbUser, String dbPass) {
        try {
            System.out.print("Enter user ID: ");
            long userId = Long.parseLong(reader.readLine());
            System.out.print("New password: ");
            String newPassword = reader.readLine();

            String sql = "UPDATE account SET password = ? WHERE user_id = ?";
            try (
                    Connection databaseConnection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
                    PreparedStatement updatePassword = databaseConnection.prepareStatement(sql)
            ) {
                updatePassword.setString(1, newPassword);
                updatePassword.setLong(2, userId);

                int rowsAffected = updatePassword.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Password updated!");  // Bekräftelse som testet letar efter
                } else {
                    System.out.println("User not found.");
                }
            }
        } catch (SQLException | IOException | NumberFormatException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void deleteAccount(BufferedReader reader, String jdbcUrl, String dbUser, String dbPass) {
        try {
            System.out.print("Enter user ID: ");
            long userId = Long.parseLong(reader.readLine());

            String sql = "DELETE FROM account WHERE user_id = ?";
            try (
                    Connection databaseConnection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
                    PreparedStatement deleteAccount = databaseConnection.prepareStatement(sql)
            ) {
                deleteAccount.setLong(1, userId);

                int rowsAffected = deleteAccount.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Account deleted!");  // Bekräftelse som testet letar efter
                } else {
                    System.out.println("User not found.");
                }
            }
        } catch (SQLException | IOException | NumberFormatException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private boolean validateLogin(String jdbcUrl, String dbUser, String dbPass, String username, String password){
        String sqlQuery = "SELECT COUNT(*) FROM account WHERE name = ? AND password = ?";

        try (
                Connection databaseConnection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(sqlQuery)
        ) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Returnera true om användaren finns
            }
        } catch (SQLException e) {
            System.err.println("Fel vid validering av inloggning: " + e.getMessage());
        }

        return false; // Returnera false om något gick fel eller om användaren inte finns
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

