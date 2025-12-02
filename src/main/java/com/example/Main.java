package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    private AccountRepository accountRepository;
    private MoonMissionRepository missionRepository;
    private Scanner scanner;


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
        this.accountRepository=new JdbcAccountRepository(jdbcUrl, dbUser, dbPass);
        this.missionRepository=new JdbcMoonMissionRepository(jdbcUrl,dbUser,dbPass);
        this.scanner=new Scanner(System.in);

        if(handleLogin()){
            showMainMenu();
        }

        scanner.close();
    }

    private boolean handleLogin() {
        boolean loggedIn=false;
        while(!loggedIn){
            System.out.println("Username: ");
            String username=scanner.nextLine();
            System.out.println("Password: ");
            String password= scanner.nextLine();

            if (accountRepository.isValidLogin(username, password)){
                System.out.println("login successful! ");
                loggedIn=true;
            } else{
                System.out.println(" Login invalid. Enter 0 to exit, or any other key to try again. ");
                String choice= scanner.nextLine().trim();
                if (choice.equals("0")){
                    return false;
                }
            }
        }
        return true;
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
