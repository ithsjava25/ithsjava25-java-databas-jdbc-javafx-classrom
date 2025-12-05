package com.example;

import com.example.jdbc.JdbcAccountRepository;
import com.example.jdbc.JdbcMoonMissionRepository;

import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private final JdbcAccountRepository accountRepo;
    private final JdbcMoonMissionRepository missionRepo;
    private final Scanner sc;

    public Main(SimpleDriverManagerDataSource dataSource) {
        this.accountRepo = new JdbcAccountRepository(dataSource);
        this.missionRepo = new JdbcMoonMissionRepository(dataSource);
        this.sc = new Scanner(System.in);
    }

    public static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }

        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        SimpleDriverManagerDataSource ds = new SimpleDriverManagerDataSource(jdbcUrl, dbUser, dbPass);

        new Main(ds).run();
    }

    public void run() {
        boolean authenticated = false;
        String username = "";

        // Loop until valid credentials
        while (!authenticated) {
            System.out.print("Enter your Username (or 0 to exit): ");
            username = sc.nextLine();
            if ("0".equals(username)) {
                System.out.println("Exiting program.");
                return;
            }

            System.out.print("Enter your password: ");
            String password = sc.nextLine();

            if (accountRepo.validateCredentials(username, password)) {
                System.out.println("Welcome / Välkommen: " + username);
                authenticated = true;
            } else {
                System.out.println("Invalid Username or Password. Try again or type 0 to exit.");
            }
        }

        // Once authenticated, show menu until user exits
        int choice;
        do {
            getOptions();

            // safer input handling
            String choiceStr = sc.nextLine();
            try {
                choice = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
                choice = -1; // invalid
            }

            options(choice);
        } while (choice != 0);

        System.out.println("Goodbye, " + username + "!");
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

    public void getOptions() {
        System.out.println(
                """
                        Select an option:
                        1 - List moon missions (prints spacecraft names from `moon_mission`).
                        2 - Get a moon mission by mission_id (prints details for that mission).
                        3 - Count missions for a given year (prompts: year; prints the number of missions launched that year).
                        4 - Create an account (prompts: first name, last name, ssn, password; prints confirmation).
                        5 - Update an account password (prompts: user_id, new password; prints confirmation).
                        6 - Delete an account (prompts: user_id; prints confirmation).
                        0 - Exit.
                        By pressing either of the numbers listed.
                """
            );
    }

    public void options(int choice) {

        switch (choice) {

            case 1: missionRepo.
                    listMissions().
                    forEach(System.out::println);
                break;

            case 2:
                System.out.print("Enter a mission ID you want to search for: ");
                int missionId = sc.nextInt();
                sc.nextLine();
                Optional<MoonMission> missionOpt = missionRepo.getMissionById(missionId);
                if (missionOpt.isPresent()) {
                    MoonMission mission = missionOpt.get();
                    System.out.printf(
                            "Mission ID: %d%nSpacecraft: %s%nLaunch Date: %s%nCarrier Rocket: %s%nOutcome: %s%nMission Type: %s%nOperator: %s%n",
                            mission.getMissionId(),
                            mission.getSpacecraft(),
                            mission.getLaunchDate(),
                            mission.getCarrierRocket(),
                            mission.getOutcome(),
                            mission.getMissionType(),
                            mission.getOperator()
                    );
                } else {
                    System.out.println("No mission found.");
                }
                break;

            case 3:
                System.out.print("Enter the year you wish to see the number of missions: ");
                int year = sc.nextInt();
                sc.nextLine();
                int numOfMissions = missionRepo.countMissionsByYear(year);
                System.out.println("There were: " + numOfMissions + " year " + year);
                break;

            case 4:
                System.out.println("Enter your first name: ");
                String firstName = sc.nextLine();
                System.out.println("Enter your last name: ");
                String lastName = sc.nextLine();
                System.out.println("Enter your ssn (social security number): ");
                String ssn = sc.nextLine();
                System.out.println("Enter your password: ");
                String password = sc.nextLine();

                if (accountRepo.createAccount(firstName, lastName, ssn, password)) {
                    System.out.println("Your account was successfuly created");
                } else {
                    System.out.println("Something went wrong during the creation of your account");
                }
                break;

            case 5:
                System.out.println("Enter the userId of the account you would like to update your password for: ");
                int userId = sc.nextInt();
                sc.nextLine();
                System.out.println("Enter your new password");
                String newPassword = sc.nextLine();

                if (accountRepo.updatePassword(userId, newPassword)) {
                    System.out.println("Your password has been updated!");
                } else {
                    System.out.println("Something went wrong during password update.");
                }
                break;

            case 6:
                System.out.println("Enter userID of the account u want deleted: ");
                int userIdDelete = sc.nextInt();
                sc.nextLine();

                boolean isDeleted = accountRepo.deleteAccount(userIdDelete);
                System.out.println(isDeleted ? "You account was successfully deleted!" : "Something went wrong deleting your account.");
                break;

            case 0:
                System.out.println("Exiting the program.");
                break;

            default:
                System.out.println("Invalid choice");
        }
    }
}
