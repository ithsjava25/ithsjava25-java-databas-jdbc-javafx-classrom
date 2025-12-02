package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
                System.out.println("Login successful! ");
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

    private void showMainMenu() {
        boolean running= true;
        while (running){
            System.out.println("\n--- Main Menu ---");
            System.out.println("1) List moon missions");
            System.out.println("2) Get a moon mission by mission_id");
            System.out.println("3) Count missions for a given year");
            System.out.println("4) Create an account");
            System.out.println("5) Update an account password");
            System.out.println("6) Delete an account");
            System.out.println("0) Exit");
            System.out.print("Choose an option: ");

            String choice=scanner.nextLine();

            try{
                switch(choice){
                    case "1": listMoonMissions(); break;
                    case "2": getMissionById(); break;
                    case "3": countMissionsByYear(); break;
                    case "4": createAccount(); break;
                    case "5": updateAccountPassword(); break;
                    case "6": deleteAccount(); break;
                    case "0": running = false; break;
                    default: System.out.println("Invalid option. Please try again."); break;

                }
            } catch (Exception e){
                //to avoid numberFormat exceptions.
                System.err.println(" An error occurred: " + e.getMessage());
                scanner.nextLine();
            }

        }
    }




    private void listMoonMissions() {
        System.out.println("\n--- Spacecraft Names ---");
        List<String> names = missionRepository.findAllSpacecraftNames();
        names.forEach(name-> System.out.println("- " + name));
    }

    private void getMissionById() {
        System.out.println("Enter Mission ID: ");
        try{
            long id = Long.parseLong(scanner.nextLine());
            Optional<MoonMission> mission = missionRepository.findById(id);

            if (mission.isPresent()){
                MoonMission m=mission.get();

                System.out.println("\nMission details for ID " + m.missionId() + ":");
                System.out.println("  Name: " + m.spacecraftName());
                System.out.println("  Year: " + m.launchYear());
                System.out.println("  Description: " + m.description());
            } else{
                System.out.println("Mission not found.");
            }


        } catch(NumberFormatException e){
            System.out.println("Invalid ID format.");
        }
    }

    private void countMissionsByYear() {
        System.out.println(" Enter launch year: ");

        try{
            int year= Integer.parseInt(scanner.nextLine());
            int count= missionRepository.countByYear(year);

            System.out.println(" There were " + count + " missions launched in " + year + ".");
        } catch(NumberFormatException e){
            System.out.println(" Invalid year format.");
        }
    }

    private void createAccount(){
        System.out.println("\n--- Create Account ---");
        System.out.print("Enter first name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter last name: ");
        String lastName = scanner.nextLine();
        System.out.print("Enter SSN: ");
        String ssn = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if(accountRepository.createAccount(firstName,lastName,ssn,password)){
            System.out.println(" Account created successfully!");
        } else{
            System.out.println("failed to create account.");
        }

    }

    private void updateAccountPassword(){
        System.out.println("Enter user ID to update: ");

        try{
            long userId=Long.parseLong(scanner.nextLine());
            System.out.println("Enter password: ");
            String newPassword= scanner.nextLine();


            if (accountRepository.updatePassword(userId,newPassword)){
                System.out.println("Password updated successfully!");
            } else{
                System.out.println("failed to update password.");
            }
        } catch(NumberFormatException e ){
            System.out.println("Invalid User ID format.");
        }

    }

    private void deleteAccount(){
        System.out.println(" Enter User ID to delete: ");

        try{
            long userId=Long.parseLong(scanner.nextLine());

            if (accountRepository.deleteAccount(userId)){
                System.out.println("Account deleted successfully!");
            }else{
                System.out.println("failed to delete account. User id not found.");
            }

        } catch (NumberFormatException e){
            System.out.println("Invalid User ID format.");
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
