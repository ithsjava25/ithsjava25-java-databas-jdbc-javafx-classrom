package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

        AccountRepository accountRepo = new AccountRepositoryImpl(jdbcUrl, dbUser, dbPass);
        MoonMissionRepository moonMissionRepo = new MoonMissionRepositoryImpl(jdbcUrl, dbUser, dbPass);


        Scanner scanner = new Scanner(System.in);
        System.out.println("Username: ");
        String user = scanner.nextLine();
        System.out.println("Password: ");
        String pass = scanner.nextLine();
        if (user.isEmpty() || pass.isEmpty()) {
            System.out.println("Username or password cannot be empty.");
        }

        if (!accountRepo.findUsernames().contains(user) || !accountRepo.findPasswords().contains(pass)) {
            System.out.println("Invalid username or password.");
            return;
        }
        String choice;
        String menu = """
                    1) List moon missions (prints spacecraft names from `moon_mission`).
                    2) Get a moon mission by mission_id (prints details for that mission).
                    3) Count missions for a given year (prompts: year; prints the number of missions launched that year).
                    4) Create an account (prompts: first name, last name, ssn, password; prints confirmation).
                    5) Update an account password (prompts: user_id, new password; prints confirmation).
                    6) Delete an account (prompts: user_id; prints confirmation).
                    0) Exit.
                """;
        System.out.println(menu);
        choice = scanner.next();

        switch (choice) {
            case "1" -> System.out.println(moonMissionRepo.listMoonMissions());
            case "2" -> {
                System.out.println("Provide a mission_id to get information: ");
                String id = scanner.next();
                System.out.println(moonMissionRepo.getMoonMissionById(id).toString());
            }
            case "3" -> {
                System.out.println("Enter a year for which you want number of missions listed: ");
                int year = scanner.nextInt();
                System.out.println("In " + year + "there were " + moonMissionRepo.missionsCountByYear(year) + "missions.");
            }
            case "4" -> {
                String name, firstName, lastName, password, ssn;
                System.out.println("Type in you username: ");
                name = scanner.next();
                System.out.println("Type in your first name: ");
                firstName = scanner.next();
                System.out.println("Type in your last name: ");
                lastName = scanner.next();
                System.out.println("Type in your password: ");
                password = scanner.next();
                System.out.println("Type in your ssn: ");
                ssn = scanner.next();
                Account newAccount = new Account(name, password, firstName, lastName, ssn);
                if (accountRepo.createAccount(newAccount)) {
                    System.out.println("Account successfully created.");
                    System.out.println(accountRepo.countAccounts());
                } else {
                    System.out.println("Account could not be created.");
                }
            }
            case "5" -> {
                System.out.println("Enter a user_id to update the password for: ");
                int userId = scanner.nextInt();
                System.out.println("Enter new password:");
                String newPassword = scanner.next();
                while (newPassword.isEmpty()) {
                    System.out.println("Password cannot be empty.");
                    System.out.println("Enter new password:");
                    newPassword = scanner.next();
                }
                if(accountRepo.updatePassword(userId, newPassword))
                    System.out.println("Password successfully updated.");
                else
                    System.out.println("Password could not be updated.");
            }
            case "6" -> {
                System.out.println("Enter the user_id for the account you want to delete: ");
                int userId = scanner.nextInt();
                if(accountRepo.deleteAccount(userId))
                    System.out.println("Account successfully deleted.");
                else
                    System.out.println("Account could not be deleted.");

            }
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
