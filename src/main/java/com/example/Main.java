package com.example;

import repositories.AccountRepo;
import repositories.MoonMissionRepo;

import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    private final Scanner scanner = new Scanner(System.in);

    static void main(String[] args) throws SQLException {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();

    }

    public void run() {
        if (DataSource.getJdbcUrl() == null || DataSource.getDbUser() == null || DataSource.getDbPass() == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        AccountRepo accountRepo = new AccountRepo();
        MoonMissionRepo moonMissionRepo = new MoonMissionRepo();

        //Only run program if sign-in is valid
        boolean executeProgram = validateSignIn(accountRepo);
        while (executeProgram) {

            displayMenu();
            System.out.print("Your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "0" -> executeProgram = false;
                case "1" -> displayAllMoonMissions(moonMissionRepo);
                case "2" -> getMissionFromID(moonMissionRepo);
                case "3" -> displayMissionsForAYear(moonMissionRepo);
                case "4" -> createAccount(accountRepo);
                case "5" -> updatePassword(accountRepo);
                case "6" -> deleteAccount(accountRepo);
                default -> System.out.println("Invalid entry, please try again.");
            }
        }
    }

    public void createAccount(AccountRepo accountRepo) {
        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine().trim();
        if(firstName.isBlank() || firstName.length() < 3){
            System.out.println("Error. First name must be at least 3 characters.");
            return;
        }

        System.out.print("Enter your lastname: ");
        String lastName = scanner.nextLine().trim();
        if(lastName.isBlank() || lastName.length() < 3){
            System.out.println("Error. Lastname must be at least 3 characters.");
            return;
        }

        System.out.print("Enter your social security number (yymmdd-nnnn): ");
        String ssn = scanner.nextLine().trim();
        if(!ssn.matches("^\\d{6}-\\d{4}")){
            System.out.println("Error! Must be in format yymmdd-xxxx");
            return;
        }

        System.out.print("Choose a password: ");
        String password = scanner.nextLine().trim();
        if(!validPassword(password)){
            return;
        }

        accountRepo.createAccount(firstName, lastName, password, ssn);
    }

    private void deleteAccount(AccountRepo accountRepo) {
        System.out.print("Enter userId for the account that you would like to delete: ");
        int userId = Integer.parseInt(scanner.nextLine().trim());

        accountRepo.deleteAccount(userId);
    }

    private boolean validPassword(String password){
        if(password.isBlank() || password.length() < 6){
            System.out.println("Error. Password must be at least 8 characters long.");
            return false;
        }
        return true;
    }

    private void updatePassword(AccountRepo accountRepo) {
        System.out.print("Enter user id: ");
        int userId = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Choose a new password: ");
        String password = scanner.nextLine().trim();
        if(!validPassword(password)){
            return;
        }

        accountRepo.updatePassword(userId, password);
    }

    private void displayMissionsForAYear(MoonMissionRepo moonMissionRepo) {
        System.out.print("Enter a year: ");
        String year = scanner.nextLine().trim();
        if(!year.matches("^[1-2][09]\\d{2}")){
            System.out.println("Invalid year");
            return;
        }

        moonMissionRepo.allMissionsConductedInYear(year);
    }

    private void getMissionFromID(MoonMissionRepo moonMissionRepo) {
        System.out.print("Enter mission-id: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        moonMissionRepo.getMissionFromID(id);
        System.out.println();
    }

    private void displayAllMoonMissions(MoonMissionRepo moonMissionRepo) {
        String column = "spacecraft";
        System.out.println("-----Spacecrafts-----\n");
        moonMissionRepo.displayColumn(column);
        System.out.println();
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

    private boolean validateSignIn(AccountRepo accountRepo){
        while (true) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            if(accountRepo.validateLogIn(username, password))
                return true;

            System.out.print("Invalid username or password. Press 0 to exit or any other key to return to sign in: ");
            String choice = scanner.nextLine().trim();
            System.out.println();
            if (choice != null && choice.trim().equals("0"))
                return false;
        }
    }

    private static void displayMenu() {
        System.out.println("            MENU\n" +
                "==================================\n" +
                "\n" +
                "1) List moon missions\n" +
                "2) Get a moon mission by mission_id\n" +
                "3) Count missions for a given year\n" +
                "4) Create an account\n" +
                "5) Update an account password\n" +
                "6) Delete an account\n" +
                "0) Exit\n" +
                " ");
    }
}