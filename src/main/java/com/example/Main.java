package com.example;

import java.sql.*;
import java.util.Arrays;
import java.util.Objects;

public class Main {
    private static String userQuery = "SELECT * FROM account WHERE name = ?";

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


        if(validateSignIn() == false){
            System.out.println("Invalid username or password");
        } else {
            System.out.println("YAY");
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


    private static boolean validateSignIn(){
        String userName = IO.readln("Enter the username: ");
        String password = IO.readln("Enter the password: ");

        try(Connection con = DataSource.getConnection(); PreparedStatement ps = con.prepareStatement(userQuery)){
            ps.setString(1, userName);
            ResultSet result = ps.executeQuery();
            if(result.next() == false){
                return false;
            }

            String inputPassword = result.getString(3);
            return Objects.equals(inputPassword, password);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private static void printMenu(){
        System.out.println("                         MENU\n" +
                "=====================================================================\n" +
                "\n" +
                "1) List moon missions (prints spacecraft names from `moon_mission`).\n" +
                "2) Get a moon mission by mission_id (prints details for that mission).\n" +
                "3) Count missions for a given year (prompts: year; prints the number of missions launched that year).\n" +
                "4) Create an account (prompts: first name, last name, ssn, password; prints confirmation).\n" +
                "5) Update an account password (prompts: user_id, new password; prints confirmation).\n" +
                "6) Delete an account (prompts: user_id; prints confirmation).\n" +
                "0) Exit.");
    }
}