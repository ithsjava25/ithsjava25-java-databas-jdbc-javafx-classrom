package com.example;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    private static final String userQuery = "SELECT * FROM account WHERE name = ?";
    private static String missionQuery = "SELECT * FROM moon_mission";
    private static final String COUNT_MISSIONS_QUERY = "SELECT COUNT(*) FROM moon_mission WHERE launch_date LIKE ?";

    private static final int columnsMoonMissions = 7;
    private Scanner scanner = new Scanner(System.in);

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

        boolean executeProgram = authenticateUser();
        while(executeProgram){

            displayMenu();
            System.out.println("Your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "0":
                    executeProgram = false;
                    break;
                case "1":
                    displayAllMoonMissions();
                    break;
                case "2":
                    getMissionFromID();
                    break;
                case "3":
                    displayMissionsForAYear();
                    break;
                case "4":
                    createAccount();
                    break;
                case "5":
                    updatePassword();
                    break;
                case "6":
                    deleteAccount();
                    break;
                default:
                    System.out.println("Invalid entry, please try again.");
                    break;

            }
        }
    }

    public void createAccount(){
        String addUser = "insert into account (name, password, first_name, last_name, ssn)" +
                         "values (?, ?, ?, ?, ?)";

        try(Connection con = DataSource.getConnection(); PreparedStatement pS = con.prepareStatement(addUser)) {
            System.out.println("Enter your firstname: ");
            String firstname = scanner.nextLine();
            System.out.println("Enter your lastname: ");
            String lastname = scanner.nextLine();
            System.out.println("Choose a password: ");
            String password = scanner.nextLine();
            System.out.println("Enter your social security number (nnnnnn-nnnn): ");
            String ssn = scanner.nextLine();
            String username = firstname.substring(0, 3) + lastname.substring(0, 3);

            pS.setString(1, username);
            pS.setString(2, password);
            pS.setString(3, firstname);
            pS.setString(4, lastname);
            pS.setString(5, ssn);
            pS.executeUpdate();

            System.out.println("User " + username + " succesfully created");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteAccount(){
        String delete = "delete from account where user_id = ?";
        try(Connection con = DataSource.getConnection(); PreparedStatement pS = con.prepareStatement(delete)) {
            System.out.println("Enter user_id for the account that you would like to delete: ");
            int id = Integer.parseInt(scanner.nextLine());

            pS.setInt(1, id);
            int rowsDeleted = pS.executeUpdate();

            if (rowsDeleted > 0)
                System.out.println("Account with id " + id + " successfully deleted");
            else
                System.out.println("Error! Something went wrong when deleting the account.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updatePassword(){
        String findId = "update account set password = ? where user_id = ?";
        try(Connection con = DataSource.getConnection(); PreparedStatement pS = con.prepareStatement(findId)) {
            System.out.print("Enter user-id: ");
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("New password: ");
            String password = scanner.nextLine();

            pS.setString(1, password);
            pS.setInt(2, id);
            int rowsUpdated = pS.executeUpdate();

            if (rowsUpdated > 0)
                System.out.println("Password successfully updated.");
            else
                System.out.println("Error! Something went wrong when updating the password.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void displayMissionsForAYear() {
        try(Connection con = DataSource.getConnection(); PreparedStatement pS = con.prepareStatement(COUNT_MISSIONS_QUERY)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String inputYear = IO.readln("Select a year: ");
            LocalDate year = LocalDate.parse(inputYear.trim() + "-01-01", formatter);

            pS.setDate(1, Date.valueOf(year));
            ResultSet result = pS.executeQuery();

            if(result.next()){
                int numOfMissions = result.getInt("mission_count");
                System.out.println("During " + year + "there were " + numOfMissions + " missions in total.");
            } else
                System.out.println("There were no moon missions registered during " + year);


        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    private void getMissionFromID(){
        try(Connection con = DataSource.getConnection(); PreparedStatement pS = con.prepareStatement(missionQuery + " where mission_id = ?")) {
            System.out.println("Enter a mission-id: ");
            int missionId = Integer.parseInt(scanner.nextLine());
            pS.setInt(1, missionId);
            ResultSet result = pS.executeQuery();

            if(result.next()){
                ResultSetMetaData meta = result.getMetaData();
                int columns = meta.getColumnCount();

                System.out.println("\n-------- Mission Details --------\n");
                for(int i = 1; i <= columns; i++){
                    System.out.println(meta.getColumnName(i) + ": " + result.getString(i));
                }

            } else {
                System.out.println("Could not find mission with id: " + missionId);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void displayAllMoonMissions(){
        try(Connection con = DataSource.getConnection(); PreparedStatement pS = con.prepareStatement(missionQuery)) {
            ResultSet result = pS.executeQuery();
            System.out.println("\nList of all spacecrafts: \n ");
            while (result.next()){
                String name = result.getString("spacecraft");
                System.out.println(name);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
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

    private boolean authenticateUser(){
        while (true){
            System.out.println("Username: ");
            String username = scanner.nextLine();
            System.out.println("Password: ");
            String password = scanner.nextLine();
            System.out.println();

            if(validateSignIn(username, password))
                return true;

            System.out.println("Invalid username or password. Press 0 to exit or any other key to return to sign in: ");
            String choice = scanner.nextLine();
            if(choice != null && choice.trim().equals("0"))
                return false;
        }
    }

    private static boolean validateSignIn(String username, String password){
        try(Connection con = DataSource.getConnection(); PreparedStatement pS = con.prepareStatement(userQuery)) {
            pS.setString(1, username);
            ResultSet result = pS.executeQuery();
            if(result.next()){
                String inputPassword = result.getString(3);
                return Objects.equals(inputPassword, password);
            }

            return false;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void displayMenu(){
        System.out.println("                         MENU\n" +
                "=====================================================================\n" +
                "\n" +
                "1) List moon missions (prints spacecraft names from `moon_mission`).\n" +
                "2) Get a moon mission by mission_id (prints details for that mission).\n" +
                "3) Count missions for a given year (prompts: year; prints the number of missions launched that year).\n" +
                "4) Create an account (prompts: first name, last name, ssn, password; prints confirmation).\n" +
                "5) Update an account password (prompts: user_id, new password; prints confirmation).\n" +
                "6) Delete an account (prompts: user_id; prints confirmation).\n" +
                "0) Exit.\n" +
                " ");
    }
}