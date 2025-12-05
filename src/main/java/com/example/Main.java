package com.example;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

        //Todo: Starting point for your code

        DataSource dataSource = new SimpleDriverManagerDataSource(jdbcUrl, dbUser, dbPass);
        AccountRepository accountRepo = new AccountRepositoryImpl(dataSource);
        MoonMissionRepository moonMissionRepo = new MoonMissionRepositoryImpl(dataSource);

        Scanner scanner = new Scanner(System.in);

        if (validateLogin(accountRepo, scanner)) return;

        showMenu();
            String choice = scanner.nextLine();
            if (!choice.isEmpty()) {
                switch (choice) {
                    case "1" -> showMissions(moonMissionRepo);
                    case "2" -> getMissionById(scanner, moonMissionRepo);
                    case "3" -> countMissionsForYear(scanner, moonMissionRepo);
                    case "4" -> createAccount(scanner, accountRepo);
                    case "5" -> updatePasswordById(scanner, accountRepo);
                    case "6" -> deleteAccountById(scanner, accountRepo);
                    case "0" -> System.out.println("Exiting...");
                    default -> System.out.println("Invalid choice.");
                }
            } else {
                System.out.println("Choice cannot be empty.");
            }

//        scanner.close();
    }

    private boolean validateLogin(AccountRepository accountRepo, Scanner scanner) {
        Optional<Account> currentUser = login(accountRepo, scanner);

        while (currentUser.isEmpty()) {
            System.out.println("Enter 0 to exit or 1 to try again.");
            String answer = scanner.nextLine().trim();
            if (answer.equals("0")) {
                System.out.println("Exiting...");
                return true;
            } else if (answer.equals("1")) {
                currentUser = login(accountRepo, scanner);
            }else
                System.out.println("Invalid choice.");
        }
        return false;
    }

    private Optional<Account> login(AccountRepository accountRepo, Scanner sc) {
        System.out.println("Username: ");
        String user = sc.nextLine().trim();
        System.out.println("Password: ");
        String pass = sc.nextLine().trim();
        if (user.isEmpty() || pass.isEmpty()) {
            System.out.println("Username or password cannot be empty.");
            return Optional.empty();
        }

        Optional<Account> maybeAccount = accountRepo.findByUsername(user);
        if(maybeAccount.isEmpty() || !pass.equals(maybeAccount.get().getPassword())) {
            System.out.println("Invalid username or password.");
            return Optional.empty();
        }
        return maybeAccount;
    }

    private static void deleteAccountById(Scanner sc, AccountRepository accountRepo) {
        System.out.println("Enter the user_id for the account you want to delete: ");
        int userId = Integer.parseInt(sc.nextLine().trim());
        if(accountRepo.deleteAccount(userId))
            System.out.println("Account successfully deleted.");
        else
            System.out.println("Account could not be deleted.");
    }

    private static void updatePasswordById(Scanner sc, AccountRepository accountRepo) {
        System.out.println("Enter a user_id to update the password for: ");
        int userId = Integer.parseInt(sc.nextLine().trim());
        System.out.println("Enter new password:");
        String newPassword = sc.nextLine().trim();
        while (newPassword.isEmpty()) {
            System.out.println("Password cannot be empty.");
            System.out.println("Enter new password:");
            newPassword = sc.nextLine().trim();
        }
        if(accountRepo.updatePassword(userId, newPassword))
            System.out.println("Password successfully updated.");
        else
            System.out.println("Password could not be updated.");
    }

    private static void createAccount(Scanner sc, AccountRepository accountRepo) {
        String name, firstName, lastName, password, ssn;
        System.out.println("Type in your username: ");
        name = sc.nextLine().trim();
        System.out.println("Type in your first name: ");
        firstName = sc.nextLine().trim();
        System.out.println("Type in your last name: ");
        lastName = sc.nextLine().trim();
        System.out.println("Type in your password: ");
        password = sc.nextLine().trim();
        System.out.println("Type in your ssn: ");
        ssn = sc.nextLine().trim();
        Account newAccount = new Account(name, password, firstName, lastName, ssn);
        if (accountRepo.createAccount(newAccount)) {
            System.out.println("Account successfully created.");
        } else {
            System.out.println("Account could not be created.");
        }
    }

    private static void countMissionsForYear(Scanner sc, MoonMissionRepository moonMissionRepo) {
        System.out.println("Enter a year for which you want number of missions listed: ");
        int year = Integer.parseInt(sc.nextLine().trim());
        System.out.println("In " + year + " there were " + moonMissionRepo.missionsCountByYear(year) + " missions.");
    }

    private static void getMissionById(Scanner sc, MoonMissionRepository moonMissionRepo) {
        System.out.println("Provide a mission_id to get information: ");
        String id = sc.nextLine().trim();
        List<MoonMission> missions = moonMissionRepo.getMoonMissionById(id);
        if(missions.isEmpty())
            System.out.println("Mission not found.");
        else
            System.out.println(missions);
    }

    private static void showMissions(MoonMissionRepository moonMissionRepo) {
        System.out.println(moonMissionRepo.listMoonMissions());
    }

    private static void showMenu() {
        System.out.println("Options: ");
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
        System.out.println("Enter your option (0-6): ");
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
