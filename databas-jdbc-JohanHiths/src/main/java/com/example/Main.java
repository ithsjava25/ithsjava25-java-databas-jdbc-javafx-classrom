package com.example;


import org.mindrot.jbcrypt.BCrypt;
import java.util.List;
import java.sql.*;
import java.util.Arrays;

public class Main {
    public void runApplicationMenu(Connection connection) throws SQLException {
        MoonMissionRepository missionRepo = new MoonMissionRepositoryJdbc(connection);
        AccountRepository accountRepo = new JdbcAccountRepository(connection);
        boolean isRunning = true;
        while (isRunning) {
            System.out.println("1) List moon missions (prints spacecraft names from `moon_mission`).\n" +
                    "   2) Get a moon mission by mission_id (prints details for that mission).\n" +
                    "   3) Count missions for a given year (prompts: year; prints the number of missions launched that year).\n" +
                    "   4) Create an account (prompts: first name, last name, ssn, password; prints confirmation).\n" +
                    "   5) Update an account password (prompts: user_id, new password; prints confirmation).\n" +
                    "   6) Delete an account (prompts: user_id; prints confirmation).\n" +
                    "   0) Exit.");
            int choice;

            try {
                choice = Integer.parseInt(IO.readln("Enter choice: "));
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice.");
                continue;
            }
            switch (choice) {
                case 0:
                    isRunning = false;
                    break;
                case 1:
                    List<MoonMission> missions = missionRepo.listAllMissions();
                    System.out.println("\nMoon Missions");
                    for (MoonMission m : missions) {
                        System.out.println(m.getSpacecraft());
                    }

                    break;
                case 2:
                    System.out.print("Enter the mission ID: ");
                    String input = IO.readln();
                    int missionId = Integer.parseInt(input);

                    MoonMission mission = missionRepo.findMoonMissionById(missionId);

                    if (mission == null) {
                        System.out.println(" Mission not found.");
                    } else {
                        System.out.println("\n--- Mission Details ---");
                        System.out.println("ID: " + mission.getMissionId());
                        System.out.println("Spacecraft: " + mission.getSpacecraft());
                        System.out.println("Launch date: " + mission.getLaunchDate());
                        System.out.println("Outcome: " + mission.getOutcome());
                        System.out.println("Carrier rocket: " + mission.getCarrierRocket());
                        System.out.println("------------------------");
                    }
                    break;
                case 3:
                    int year = 0;
                    while (true) {
                        try {
                            String yearInput = IO.readln("Enter the launch year: ");
                            year = Integer.parseInt(yearInput);
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid year. Please enter a numeric value.");
                        }
                    }

                    int count = missionRepo.countMissionsByYear(year);

                    System.out.println("Number of missions launched in " + year + ": " + count);
                    break;


                case 4:
                    System.out.println("Enter first name");
                    String firstName = IO.readln();
                    System.out.println("Enter last name");
                    String lastName = IO.readln();
                    System.out.println("Enter SSN");
                    String ssn = IO.readln();
                    System.out.println("Enter password");
                    String rawPassword = IO.readln();



                    String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
                    boolean accountCreated = accountRepo.createAccount(firstName, lastName, ssn, hashedPassword);

                    if (accountCreated) {
                        System.out.println("Account created successfully.");
                    } else {
                        System.out.println("Account creation failed.");
                    }

                    break;
                case 5:
                    while (true) {
                        System.out.println("Enter the usedId to update password");
                        String idInput = IO.readln();

                        int userId;
                        try {
                            userId = Integer.parseInt(idInput);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid user ID.");
                            break;
                        }

                        System.out.println("Enter new password");
                        String newPassword = IO.readln();
                        if(newPassword.equals("0")){
                            break;
                        }

                        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());

                        boolean updatePassword = accountRepo.updatePassword(userId, hashed);

                        if (updatePassword) {
                            System.out.println("Password updated successfully.");

                        } else {
                            System.out.println("Password update failed.");
                            break;
                        }
                    }
                    break;
                case 6:
                    System.out.println("Enter user ID to delete!");
                    String deleteInput = IO.readln();

                    int deleteId;
                    try {
                        deleteId = Integer.parseInt(deleteInput);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid user ID.");
                        break;
                    }
                    boolean deleted = accountRepo.deleteAccount(deleteId);
                    if (deleted) {
                        System.out.println("Account deleted successfully.");
                    } else{
                        System.out.println("Account delete failed.");
                    }

                    break;
            }

        }

    }



    static void main(String[] args) throws SQLException {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();


    }

    public void run() throws SQLException {


        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {

            while (true) {

                String username = IO.readln("Username: ");
                String password = IO.readln("Password: ");

                String query = "SELECT * FROM account WHERE name = ? AND password = ?";

                try (PreparedStatement pstmt = connection.prepareStatement(query)) {

                    pstmt.setString(1, username);
                    pstmt.setString(2, password);

                    try (ResultSet rs = pstmt.executeQuery()) {

                        if (rs.next()) {
                            System.out.println("Logged in!");
                            runApplicationMenu(connection);
                            return; // exit login
                        } else {
                            System.out.println("Invalid username or password");
                        }
                    }

                } catch (SQLException e) {
                    System.err.println("Database error during login: " + e.getMessage());
                }
            }
        }
    }
    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode"))  //Add VM option -DdevMode=true
            return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE")))  //Environment variable DEV_MODE=true
            return true;
        return Arrays.asList(args).contains("--dev"); //Argument --dev
    }


    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }

    private static void printMoonMissions(Connection connection) throws SQLException {
        String moonQuery = "SELECT spacecraft FROM moon_mission";

        try (PreparedStatement pstmt = connection.prepareStatement(moonQuery);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n=== Moon Missions ===");
            while (rs.next()) {
                System.out.println("- " + rs.getString("spacecraft"));
            }


        }

    }


    private static void printMoonMissionId(Connection connection) throws SQLException {


        int missionId;
        while (true) {
            try {
                String input = IO.readln("Enter the mission ID: ");
                missionId = Integer.parseInt(input);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid mission ID (number).");
            }
        }

        String sql = "SELECT * FROM moon_mission WHERE mission_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, missionId);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    System.out.println("\n=== Mission Details ===");
                    System.out.println("Mission ID: " + rs.getInt("mission_id"));
                    System.out.println("Spacecraft: " + rs.getString("spacecraft"));
                    System.out.println("Launch Date: " + rs.getDate("launch_date"));
                    System.out.println("Carrier Rocket: " + rs.getString("carrier_rocket"));
                    System.out.println("Operator: " + rs.getString("operator"));
                    System.out.println("Mission Type: " + rs.getString("mission_type"));
                    System.out.println("Outcome: " + rs.getString("outcome"));
                } else {
                    System.out.println("No mission found with ID " + missionId);
                }
            }
        }
    }

    private static void printMissionYear(Connection connection) throws SQLException {
        int missionYear;
        while (true) {
            try {
                String input = IO.readln("Enter mission year (number)");
                missionYear = Integer.parseInt(input);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter mission year (number).");
            }
        }





        String moonDate = "SELECT count(*) FROM moon_mission WHERE YEAR(launch_date) = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(moonDate)) {
            pstmt.setInt(1, missionYear);

            try (ResultSet rs = pstmt.executeQuery()) {



                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("\nMission Type: " + missionYear + ": " + count);
                }
            }
        }
    }

    private static void printAccountCreation(Connection connection) throws SQLException {
        System.out.println("First name");
        String firstName = IO.readln();
        System.out.println("Last name");
        String lastName = IO.readln();
        System.out.println("SSN");
        String SSN = IO.readln();
        System.out.println("Password");
        String password = IO.readln();


        //String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        String sql = "INSERT INTO account (first_name, last_name, ssn, password) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, SSN);
            pstmt.setString(4, password);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("\nAccount Created");
            } else {
                System.out.println("\nAccount Creation Failed");
            }
        }
    }

    private static void printAccountUpdate(Connection connection) throws SQLException {
        System.out.println("First name");
        String firstName = IO.readln();
        System.out.println("Last name");
        String lastName = IO.readln();
        System.out.println("SSN");
        String SSN = IO.readln();
        System.out.println("Password");
        String password = IO.readln();

        String account = "Update account SET password = ?, ssn ? WHERE last_name = ?";
        ;

        try (PreparedStatement pstmt = connection.prepareStatement(account)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, SSN);
            pstmt.setString(4, password);

            int rs = pstmt.executeUpdate();
            {
                if (rs > 0) {
                    System.out.println("\nAccount Updated");
                } else {
                    System.out.println("\nAccount Update Failed");
                }
            }
        }
    }

    private static void printAccountDeletion(Connection connection) throws SQLException {
        int accountId;
        while (true) {
            try {
                String input = IO.readln("Enter the user ID to delete: ");
                accountId = Integer.parseInt(input);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid user ID (number).");
            }
        }

        String sql = "DELETE FROM account WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(String.valueOf(sql))) {
            pstmt.setInt(1, accountId);
            ;

            int rs = pstmt.executeUpdate();
            {
                if (rs > 0) {
                    System.out.println("\nAccount Id" + accountId + "removed");
                } else {
                    System.out.println("\nAccount Id" + accountId + "Removal Failed");
                }
            }
        }
    }
}