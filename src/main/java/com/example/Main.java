package com.example;

import java.io.Console;
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

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //Todo: Starting point for your code

        boolean loggedIn = false;
        InputStream in = System.in;

        while (!loggedIn) {
            try {
                System.out.print("Username: ");
                String username = readLine(in);

                System.out.print("Password: ");
                String password = readLine(in);

                try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
                     PreparedStatement ps = connection.prepareStatement(
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

            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException("I/O error reading input", e);
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