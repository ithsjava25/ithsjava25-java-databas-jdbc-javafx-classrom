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

        try (Connection connection =
                     DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {

            //Todo: Starting point for your code
            runApplication(connection);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Scanner scanner = new Scanner(System.in);
    private boolean exitAfterLogin = false;

    private void runApplication(Connection connection) throws SQLException {
        if (loginFlow(connection)) {
            menuLoop(connection);
        }
    }

    //User login
    private boolean loginFlow(Connection connection) throws SQLException {
        while (true) {
            System.out.print("Input username: ");
            String username = scanner.nextLine().strip();

            System.out.print("Input password: ");
            String password = scanner.nextLine().strip();

            if (checkCredentials(connection, username, password)) {
                System.out.println("Login Successful!");
                System.out.println();
                return true;
            }

            //If the login is invalid, print a message containing the word invalid and allow exiting via option 0
            System.out.print("Credentials invalid. Try again (Enter) or Exit? (0): ");
            if ("0".equals(scanner.nextLine().strip())) {
                exitAfterLogin = true;
                return false;
            }
        }
    }

    //Used for a lab exercise
    //Check of user credentials
    private boolean checkCredentials(Connection connection, String user, String pass)
            throws SQLException {

        String sql = "SELECT 1 FROM account WHERE name=? AND password=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, pass);
            return ps.executeQuery().next();
        }
    }

    private void menuLoop(Connection connection) throws SQLException {
        boolean running = !exitAfterLogin;

        while (running) {
            printMenu();
            System.out.print("Choose option: ");
            String command = scanner.nextLine().strip();

            switch (command) {
                case "1", "list" -> listMissions(connection);
                case "2", "get" -> getMissionById(connection);
                case "3", "count" -> countMissionsByYear(connection);
                case "4", "create" -> createAccount(connection);
                case "5", "update" -> updateAccountPassword(connection);
                case "6", "delete" -> deleteAccount(connection);
                case "0", "exit" -> running = false;
                default -> System.out.println("Invalid choice.");
            }

            System.out.println();
        }
    }

    //Menu with options
    private void printMenu() {
        System.out.println("""
                ==========================
                1) List moon missions
                2) Get mission by id
                3) Count missions by year
                4) Create account
                5) Update account password
                6) Delete account
                0) Exit
                ==========================
                """);
    }

    private void listMissions(Connection connection) throws SQLException {
        String sql = "SELECT spacecraft FROM moon_mission";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                System.out.println(rs.getString("spacecraft"));
            }
        }
    }

    private void getMissionById(Connection connection) throws SQLException {
        System.out.print("Mission id: ");
        String id = scanner.nextLine();

        String sql = "SELECT * FROM moon_mission WHERE mission_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    System.out.print(rs.getString(i) + " ");
                }
                System.out.println();
            }
        }
    }

    private void countMissionsByYear(Connection connection) throws SQLException {
        System.out.print("Year: ");
        String year = scanner.nextLine();

        String sql = "SELECT COUNT(*) FROM moon_mission WHERE YEAR(launch_date)=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, year);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Missions launched in " + year + ": " + rs.getInt(1));
            }
        }
    }

    private void createAccount(Connection connection) throws SQLException {
        String first = readRequired("First name");
        String last = readRequired("Last name");
        String ssn = readRequired("SSN");
        String password = readRequired("Password");

        String username =
                first.substring(0, Math.min(2, first.length())) +
                        last.substring(0, Math.min(2, last.length()));

        String checkSql = "SELECT COUNT(*) FROM account WHERE name=? OR ssn=?";
        try (PreparedStatement ps = connection.prepareStatement(checkSql)) {
            ps.setString(1, username);
            ps.setString(2, ssn);

            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Account already exists.");
                return;
            }
        }

        String insertSql = """
                INSERT INTO account (first_name, last_name, ssn, password, name)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setString(1, first);
            ps.setString(2, last);
            ps.setString(3, ssn);
            ps.setString(4, password);
            ps.setString(5, username);
            ps.executeUpdate();
        }

        System.out.println("Account successfully created.");
    }

    private void updateAccountPassword(Connection connection) throws SQLException {
        System.out.print("User id: ");
        String id = scanner.nextLine();

        if (!accountExists(connection, id)) {
            System.out.println("User not found.");
            return;
        }

        //Ask for a valid password ie not blank
        String newPassword = readRequired("New password");

        String sql = "UPDATE account SET password=? WHERE user_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, id);
            ps.executeUpdate();
        }

        System.out.println("Password updated.");
        System.out.println("Press Enter to continue");
        scanner.nextLine();
    }

    private void deleteAccount(Connection connection) throws SQLException {
        System.out.print("User id to delete: ");
        String id = scanner.nextLine();

        if (!accountExists(connection, id)) {
            System.out.println("Account does not exist.");
            return;
        }

        String sql = "DELETE FROM account WHERE user_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            System.out.println("Deleted rows: " + rows);
        }
    }

    private boolean accountExists(Connection connection, String id) throws SQLException {
        String sql = "SELECT 1 FROM account WHERE user_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeQuery().next();
        }
    }

    private String readRequired(String label) {
        while (true) {
            System.out.print(label + ": ");
            String value = scanner.nextLine().strip();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println(label + " cannot be empty.");
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
        return Arrays.asList(args).contains("--dev");  //Argument --dev
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
