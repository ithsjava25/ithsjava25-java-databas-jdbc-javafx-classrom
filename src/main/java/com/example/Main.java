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

        try (Scanner sc = new Scanner(System.in);
             Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
            if(!loginHandler(connection, sc)){
                return;
            }
            menuHandler(connection, sc);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //Todo: Starting point for your code

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

    private boolean loginHandler(Connection conn, Scanner sc) throws SQLException {
        System.out.println("Username: ");
        String user = sc.nextLine().trim();
        System.out.println("Password: ");
        String password = sc.nextLine().trim();

        String sql = """
                SELECT user_id
                FROM account
                WHERE name = ? AND password = ?
                """;
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, user);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return true;
            }
            System.out.println("Invalid username or password");
            return false;
        }
    }

    private void menuHandler(Connection connection, Scanner sc) throws SQLException{

        while(true){

            System.out.println("-----Menu options-----");
            System.out.println("1. List moon missions (prints spacecraft names from `moon_mission`)");
            System.out.println("2. Get a moon mission by mission_id (prints details for that mission)");
            System.out.println("3. Count missions for a given year (prompts: year; prints the number of missions launched that year)");
            System.out.println("4. Create an account (prompts: first name, last name, ssn, password; prints confirmation)");
            System.out.println("5. Update an account password (prompts: user_id, new password; prints confirmation)");
            System.out.println("6. Delete an account (prompts: user_id; prints confirmation)");
            System.out.println("0. Exit");

            System.out.println("Enter an option: ");
            int choice = Integer.parseInt(sc.nextLine().trim());

            switch (choice){
                case 1 -> listMoonMissions(connection);
                case 2 -> getMissionById(connection, sc);
                case 3 -> missionsCountByYear(connection, sc);
                case 4 -> createAccount(connection, sc);
                case 5 -> updateAccount(connection, sc);
                case 6 -> deleteAccount(connection, sc);
                case 0 -> {return;}
                default ->
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    public void listMoonMissions(Connection connection) throws SQLException{
        String sql = """
                SELECT spacecraft
                FROM moon_mission
                """;

        try(PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){

            while (rs.next()){
                String spacecraft = rs.getString("spacecraft");
                System.out.println(spacecraft);
            }
        }
    }

    public void getMissionById(Connection connection, Scanner sc) throws SQLException{
        System.out.println("MissionId: ");

        int missionId;
        while(true) {
            try {
                missionId = Integer.parseInt(sc.nextLine().trim());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        String sql = """
                SELECT *
                FROM moon_mission
                WHERE mission_id = ?
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, missionId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("=== Moon Mission Details ===");
                System.out.println("ID: " + rs.getInt("mission_id"));
                System.out.println("Spacecraft: " + rs.getString("spacecraft"));
                System.out.println("Launch Date: " + rs.getDate("launch_date"));
                System.out.println("Carrier Rocket: " + rs.getString("carrier_rocket"));
                System.out.println("Operator: " + rs.getString("operator"));
                System.out.println("Mission Type: " + rs.getString("mission_type"));
                System.out.println("Outcome: " + rs.getString("outcome"));
            } else {
                System.out.println("No missions found with ID: " + missionId);
            }
        }

    }

    public void missionsCountByYear(Connection connection, Scanner sc) throws SQLException{
        System.out.println("Launch year:");
        int launchYear = Integer.parseInt(sc.nextLine().trim());

        String sql = """
                SELECT COUNT(*) AS missionCount
                FROM moon_mission
                WHERE YEAR(launch_date) = ?
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, launchYear);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                int count = rs.getInt("missionCount");
                System.out.println("Number of missions in " + launchYear + ": " + count);
            } else{
                System.out.println("No result found.");
            }
        }
    }

    public void createAccount(Connection connection, Scanner sc) throws SQLException{
        System.out.println("First name: ");
        String firstName = sc.nextLine().trim();
        System.out.println("Last name: ");
        String lastName = sc.nextLine().trim();
        System.out.println("SSN: ");
        String ssn = sc.nextLine().trim();
        System.out.println("Password: ");
        String password = sc.nextLine().trim();
        String name = firstName.substring(0, Math.min(3, firstName.length()))
                    + lastName.substring(0, Math.min(3, lastName.length()));

        String sql = """
                INSERT INTO account (name,first_name, last_name, ssn, password)
                VALUES (?, ?, ?, ?, ?);
                """;

        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, name);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, ssn);
            ps.setString(5, password);

            ps.executeUpdate();
        }
        System.out.println("account created");
    }

    public void updateAccount(Connection connection, Scanner sc) throws SQLException{
        System.out.println("User id:");
        int userId;
        try {
            userId = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid user ID.");
            return;
        }

        System.out.println("New password: ");
        String newPassword = sc.nextLine().trim();

        String sql = """
                UPDATE account
                SET password = ? 
                WHERE user_id = ?
                """;

        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, newPassword);
            ps.setInt(2, userId);

            ps.executeUpdate();
        }
        System.out.println("Password updated");
    }

    private void deleteAccount(Connection connection, Scanner sc) throws SQLException{
        System.out.println("User id:");
        int userId;
        try {
            userId = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid user ID.");
            return;
        }


        String sql = """
                DELETE FROM account
                WHERE user_id = ?
                """;
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, userId);

            ps.executeUpdate();
        }
        System.out.println("Account deleted");
    }


}



