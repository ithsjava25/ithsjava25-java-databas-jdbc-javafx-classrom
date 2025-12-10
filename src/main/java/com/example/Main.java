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
            System.out.println("Connected to DB!");
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to DB", e);
        }

        //Todo: Starting point for your code
        Scanner scanner = new Scanner(System.in);

        while (!login(this.connection, scanner)) {
            // fortsätt försöka tills login lyckas
        }

        System.out.println("Login succsessfull! let's move on to the menu...");
    }

    public static boolean login(Connection conn, Scanner scanner) {

        System.out.print("Username (or 0 to exit): ");
        String username = scanner.nextLine();

        if (username.equals("0")) {
            System.out.println("Exiting...");
            System.exit(0);
        }

        System.out.print("Password (or 0 to exit): ");
        String password = scanner.nextLine();

        if (password.equals("0")) {
            System.out.println("Exiting...");
            System.exit(0);
        }

        String sql = "SELECT * FROM account WHERE first_name = ? AND password = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                System.out.println("Invalid credentials");
                return false;
            }

            System.out.println("Login successful!");
            return true;

        } catch (SQLException e) {
            throw new RuntimeException("Login failed", e);
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
