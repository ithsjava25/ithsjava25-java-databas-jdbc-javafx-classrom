package com.example;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() {
        // Konfigurera databas
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        DataSource ds = new SimpleDriverManagerDataSource(jdbcUrl, dbUser, dbPass);
        AccountRepository accountRepo = new AccountRepository(ds);
        MoonMissionRepository missionRepo = new MoonMissionRepository(ds);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the Moon Mission CLI!");

        // ---- Login flow: prompt for username/password first ----
        String currentUser = null;
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            boolean validLogin = accountRepo.login(username, password);
            if (!validLogin) {
                System.out.println("Invalid username or password");
                return; // stop program after invalid attempt (tests provide an extra 0 which will be ignored)
            }
            currentUser = username;
            System.out.println("Login successful! Welcome, " + currentUser);
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            return;
        } catch (Exception e) {
            // In case input is exhausted
            return;
        }

        // ---- Authenticated menu ----
        boolean running = true;
        while (running) {
            System.out.println("\nMenu:");
            System.out.println("1) List moon missions");
            System.out.println("2) Get mission by id");
            System.out.println("3) Count missions for year");
            System.out.println("4) Create an account");
            System.out.println("5) Update an account password");
            System.out.println("6) Delete an account");
            System.out.println("7) Log out (switch user)");
            System.out.println("0) Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        missionRepo.listMissions(); // skriver direkt till konsolen
                        break;

                    case "2":
                        System.out.print("Mission ID: ");
                        long id = Long.parseLong(scanner.nextLine());
                        missionRepo.getMissionById(id); // skriver direkt till konsolen
                        break;

                    case "3":
                        System.out.print("Year: ");
                        int year = Integer.parseInt(scanner.nextLine());
                        missionRepo.countByYear(year); // skriver direkt till konsolen
                        break;

                    case "4":
                        System.out.print("First name: ");
                        String firstName = scanner.nextLine().trim();
                        System.out.print("Last name: ");
                        String lastName = scanner.nextLine().trim();
                        System.out.print("SSN: ");
                        String ssnNew = scanner.nextLine().trim();
                        System.out.print("Password: ");
                        String newPass = scanner.nextLine();
                        String newUsername = accountRepo.createAccount(firstName, lastName, ssnNew, newPass);
                        System.out.println("Account created. Username: " + newUsername);
                        break;

                    case "5":
                        System.out.print("User ID: ");
                        long uid = Long.parseLong(scanner.nextLine());
                        System.out.print("New password: ");
                        String passUpdate = scanner.nextLine();
                        boolean updated = accountRepo.updatePassword(uid, passUpdate);
                        System.out.println(updated ? "Password updated" : "Update failed");
                        break;

                    case "6":
                        System.out.print("User ID to delete: ");
                        long delId = Long.parseLong(scanner.nextLine());
                        boolean deleted = accountRepo.deleteAccount(delId);
                        System.out.println(deleted ? "Account deleted" : "Delete failed");
                        break;

                    case "7":
                        System.out.print("Username: ");
                        String newUser = scanner.nextLine().trim();
                        System.out.print("Password: ");
                        String newPassLogin = scanner.nextLine();
                        try {
                            boolean ok = accountRepo.login(newUser, newPassLogin);
                            if (ok) {
                                currentUser = newUser;
                                System.out.println("Login successful! Welcome, " + currentUser);
                            } else {
                                System.out.println("Invalid username or password");
                            }
                        } catch (SQLException e) {
                            System.err.println("Database error: " + e.getMessage());
                        }
                        break;

                    case "0":
                        running = false;
                        System.out.println("Exiting program...");
                        break;

                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input format");
            }
        }

        scanner.close();
    }

    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode"))
            return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE")))
            return true;
        return Arrays.asList(args).contains("--dev");
    }

    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}