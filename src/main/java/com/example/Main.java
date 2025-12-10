package com.example;

import java.sql.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

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

            boolean authorized = login(connection);


            if (!authorized){
                System.out.println("Username or password is invalid.");
                System.exit(0);
            }

            while(true) {
                int option = promptMenu();

                switch (option) {
                    case 1 -> listMissions(connection);
                    case 2 -> getMission(connection);
                    case 3 -> missionsCountYear(connection);
                    case 4 -> createAccount(connection);
                    case 5 -> System.out.println("5");
                    case 6 -> System.out.println("6");
                    case 0 -> System.exit(0);

                    default -> System.out.println("Invalid choice.\n");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    private boolean login(Connection connection) {
        String unm = IO.readln("Username: ");
        String pw = IO.readln("Password: ");

        String accQuery = "select count(*) from account where binary name = ? and binary password = ?";
        try(PreparedStatement statement = connection.prepareStatement(accQuery)){
            statement.setString(1, unm);
            statement.setString(2, pw);

            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                if(rs.getInt("count(*)") == 1){return true;}
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int promptMenu(){
        System.out.print("\n" +
                "1) List moon missions (prints spacecraft names from `moon_mission`).\n" +
                "2) Get a moon mission by mission_id (prints details for that mission).\n" +
                "3) Count missions for a given year (prompts: year; prints the number of missions launched that year).\n" +
                "4) Create an account (prompts: first name, last name, ssn, password; prints confirmation).\n" +
                "5) Update an account password (prompts: user_id, new password; prints confirmation).\n" +
                "6) Delete an account (prompts: user_id; prints confirmation).\n" +
                "0) Exit.\n");

        return getValidInt("Enter Choice: ");
    }

    private void listMissions(Connection connection){
        String spaceshipQuery = "select spacecraft from moon_mission";

        try(PreparedStatement statement = connection.prepareStatement(spaceshipQuery)){

            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                System.out.println(rs.getString("spacecraft"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void getMission(Connection connection){
        String  missionQuery = "select * from moon_mission where mission_id = ?";
        int id = getValidInt("Mission Id: ");

        try(PreparedStatement statement = connection.prepareStatement(missionQuery)){
            statement.setInt(1, id);

            ResultSet rs = statement.executeQuery();

            if(rs.next()) {
                System.out.println(
                        "\nSpacecraft: " + rs.getString("spacecraft") +
                        "\nLaunch date: " + rs.getString("launch_date") +
                        "\nCarrier rocket: " + rs.getString("carrier_rocket") +
                        "\nOperator: " + rs.getString("operator") +
                        "\nMission type: " + rs.getString("mission_type") +
                        "\nOutcome: " + rs.getString("outcome"));
            }
            else  {
                System.out.println("\nMission not found.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void missionsCountYear(Connection connection){
        String missionYearQuery = "select count(*) from moon_mission where year(launch_date) = ?)";
        int year = getValidInt("Mission Year: ");

        try(PreparedStatement statement = connection.prepareStatement(missionYearQuery)){
            statement.setInt(1, year);

            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                System.out.println("\nMissions in " + year + ": " + rs.getInt("count(*)"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void createAccount(Connection connection) {
        String fn = getValidName("First Name: ");
        String ln = getValidName("Last Name: ");
        String ssn = getValidSSN("SSN: ");
        String pw = getValidPassword("Password: ");

        String accName;
        if(fn.length() <= 3){
            if(ln.length() <= 3){
                accName = fn + ln;
            }
            else{
                accName = fn + ln.substring(0, 2);
            }
        }
        else if(ln.length() <= 3){
           accName = fn.substring(0, 3) + ln;
        }
        else{
            accName = fn.substring(0, 3) + ln.substring(0, 3);
        }

        String checkName = "select count(*) from account where name = ?";

        while(true) {
            try (PreparedStatement statement = connection.prepareStatement(checkName)) {
                statement.setString(1, accName);

                ResultSet rs = statement.executeQuery();


                if(rs.next() && rs.getInt("count(*)") == 0){
                    break;
                }
                else{
                    accName = getValidName("Account Name: ");
                }


            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        String newAccQuery = "insert into account values (0, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(newAccQuery)) {
            statement.setString(1, accName);
            statement.setString(2, pw);
            statement.setString(3, fn);
            statement.setString(4, ln);
            statement.setString(5, ssn);

            statement.executeUpdate();


            System.out.println("\nAccount created successfully.");


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    private int getValidInt(String prompt){
        while(true){
            try {
                int option = Integer.parseInt(IO.readln("\n" + prompt));

                if (option >= 0) {
                    return option;
                }
                else  {
                    System.out.println("Please enter a positive integer.\n");
                }
            }
            catch (NumberFormatException e){
                System.out.println("Please enter a valid integer\n");
            }
        }
    }

    private String getValidName(String prompt){
        while(true){
            String name = IO.readln("\n" + prompt).trim();

            if (name.isBlank()) {
                System.out.println("\nCannot be blank");
            }
            else if(!Pattern.matches("^[a-zA-Z]+$", name)){
                System.out.println("\nMust only contain letters");
            }
            else{
                return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            }
        }
    }

    private String getValidSSN(String prompt){
        while(true){
            String ssn = IO.readln("\n" + prompt).trim();

            if (ssn.isBlank()) {
                System.out.println("\nCannot be blank");
            }
            else if(!Pattern.matches("^\\d{6}-\\d{4}$", ssn)){
                System.out.println("\nMust follow pattern YYMMDD-XXXX");
            }
            else {
                return ssn;
            }
        }
    }

    private String getValidPassword(String prompt){
        while(true){
            String pw = IO.readln("\n" + prompt);

            if(pw.length() < 6){
                System.out.println("Password must be at least 6 characters");
            }
            else{
                return pw;
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
