package com.example;

import com.example.datasource.SimpleDriverManagerDataSource;
import com.example.model.Account;
import com.example.model.MoonMission;
import com.example.repository.AccountRepository;
import com.example.repository.JdbcAccountRepository;
import com.example.repository.JdbcMoonMissionRepository;
import com.example.repository.MoonMissionRepository;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private final AccountRepository accountRepository;
    private final MoonMissionRepository missionRepository;
    private final Scanner scanner;

    public Main(AccountRepository accountRepository, MoonMissionRepository missionRepository) {
        this.accountRepository = accountRepository;
        this.missionRepository = missionRepository;
        this.scanner = new Scanner(System.in);
    }
   public static void main(String[] args) {

        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public Main() {
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS \" +\n" +
                            "\"as system properties (-Dkey=value) or environment variables.\"");
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
            System.out.println("✓ Database connection successful!");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database. Check your configuration.", e);
        }

        DataSource dataSource = new SimpleDriverManagerDataSource(jdbcUrl, dbUser, dbPass);

        this.accountRepository = new JdbcAccountRepository(dataSource);
        this.missionRepository = new JdbcMoonMissionRepository(dataSource);
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("=== Moon Mission Database === \n");

        if (!login()) {
            System.out.println("Login invalid. Exiting...");
            return;
        }
        boolean running = true;
        while (running) {
            printMenu();
            int choice = getIntInput();

            switch (choice) {
                case 1 -> listMoonMissions();
                case 2 -> getMissionById();
                case 3 -> countMissionsByYear();
                case 4 -> createAccount();
                case 5 -> updateAccountPassword();
                case 6 -> deleteAccount();
                case 0 -> {
                    running = false;
                    System.out.println("Goodbye!");
                }
                default -> System.out.println("Invalid option!");
            }
        }
        scanner.close();
    }

    private boolean login() {
        System.out.print("Username: ");
        String username  = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        Optional<Account> accountOpt = accountRepository.findByUsernameAndPassword(username, password);

        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            System.out.println("✓ Login successful! Welcome, " + account.name() + "\n");
            return true;
        } else {
            System.out.println("Invalid username or password.");
            return false;
        }
    }

    private void printMenu() {
        System.out.println("\n=== Menu ===");
        System.out.println("1) List moon missions");
        System.out.println("2) Get a moon mission by mission_id");
        System.out.println("3) Count missions for a given year");
        System.out.println("4) Create an account");
        System.out.println("5) Update an account password");
        System.out.println("6) Delete an account");
        System.out.println("0) Exit");
        System.out.print("Choose option: ");
    }
    
    private void listMoonMissions () {
        System.out.println("\n --- Moon Missions ---");

        List<String> spacecrafts = missionRepository.findAllSpaceCraftNames();
        spacecrafts.forEach(System.out::println);
    }

    private void getMissionById() {
        System.out.print("Enter mission_id: ");
        int missionId = getIntInput();

        Optional<MoonMission> missionOpt = missionRepository.findById(missionId);

        if (missionOpt.isPresent()) {
            MoonMission mission = missionOpt.get();
            System.out.println("\n--- Mission Details ---");
            System.out.println("Mission ID: " + mission.missionId());
            System.out.println("Spacecraft: " + mission.spacecraft());
            System.out.println("Launch Date: " + mission.launchDate());
            System.out.println("Carrier Rocket: " + mission.carrierRocket());
            System.out.println("Operator: " + mission.operator());
            System.out.println("Mission Type: " + mission.missionType());
            System.out.println("Outcome: " + mission.outcome());
        } else {
            System.out.println("Mission not found.");
        }
    }

    private void countMissionsByYear() {
        System.out.print("Enter year: ");
        int year = getIntInput();

        int count = missionRepository.countByYear(year);
        System.out.println("Missions launched in " + year + ": " + count);
    }

    private void createAccount() {
        System.out.print("First name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Last name: ");
        String lastName = scanner.nextLine().trim();

        System.out.print("SSN: ");
        String ssn = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        int userId = accountRepository.create(firstName, lastName, ssn, password);

        if (userId > 0) {
            System.out.println("✓ Account created successfully! User ID: " + userId);
        } else {
            System.out.println("Failed to create account.");
        }
    }

    private void updateAccountPassword() {
        System.out.print("Enter user_id: ");
        int userId = getIntInput();

        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine().trim();

        boolean success = accountRepository.updatePassword(userId, newPassword);

        if (success) {
            System.out.println("✓ Password updated successfully!");
        } else {
            System.out.println("User not found.");
        }
    }

    private void deleteAccount() {
        System.out.print("Enter user_id: ");
        int userId = getIntInput();

        boolean success = accountRepository.delete(userId);

        if (success) {
            System.out.println("✓ Account deleted successfully!");
        } else {
            System.out.println("User not found.");
        }
    }

    private int getIntInput() {
        while(true) {
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again: ");
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
