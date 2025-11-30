package com.example;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * End-to-end CLI tests for the JDBC CRUD console application.
 * <p>
 * Contract expected by these tests (implementation must follow for tests to pass):
 * - The app reads menu choices from standard input and writes results to standard output.
 * - Menu options:
 * 1. List moon missions (read-only from table `moon_mission`) and print spacecraft names.
 * 2. Create an account (table `account`): prompts for first name, last name, ssn, password.
 * 3. Update an account password: prompts for user_id and new password.
 * 4. Delete an account: prompts for user_id to delete.
 * 0. Exit program.
 * - The app should use these system properties for DB access (configured by the tests):
 * APP_JDBC_URL, APP_DB_USER, APP_DB_PASS
 * - After each operation the app prints a confirmation message or the read result.
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CliAppIT {

    @Container
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:9.5.0")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password")
            .withConfigurationOverride("myconfig")
            .withInitScript("init.sql");

    @BeforeAll
    static void wireDbProperties() {
        System.setProperty("APP_JDBC_URL", mysql.getJdbcUrl());
        System.setProperty("APP_DB_USER", mysql.getUsername());
        System.setProperty("APP_DB_PASS", mysql.getPassword());
    }

    @Test
    @Order(0)
    void testConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
                mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
        assertThat(conn).isNotNull();
    }

    @Test
    @Order(1)
    void login_withInvalidCredentials_showsErrorMessage() throws Exception {
        String input = String.join(System.lineSeparator(),
                // Expect app to prompt for username then password
                "NoUser",            // username (invalid)
                "badPassword",       // password (invalid)
                "0"                  // exit immediately after
        ) + System.lineSeparator();

        String out = runMainWithInput(input);

        assertThat(out)
                .containsIgnoringCase("Invalid username or password");
    }

    @Test
    @Order(2)
    void login_withValidCredentials_thenCanUseApplication() throws Exception {
        // Using a known seeded account from init.sql:
        // first_name = Angela, last_name = Fransson -> username (name column) = AngFra
        // password = MB=V4cbAqPz4vqmQ
        String input = String.join(System.lineSeparator(),
                "AngFra",                // username
                "MB=V4cbAqPz4vqmQ",     // password
                "1",                    // list missions after successful login
                "0"                     // exit
        ) + System.lineSeparator();

        String out = runMainWithInput(input);

        assertThat(out)
                .containsIgnoringCase("username")
                .containsIgnoringCase("password")
                .as("Expected output to contain at least one known spacecraft from seed data after successful login")
                .containsAnyOf("Pioneer 0", "Luna 2", "Luna 3", "Ranger 7");
    }

    @Test
    @Order(3)
    void listMoonMissions_printsKnownMissionNames() throws Exception {
        String input = String.join(System.lineSeparator(),
                // login first
                "AngFra",
                "MB=V4cbAqPz4vqmQ",
                "1", // list missions
                "0"  // exit
        ) + System.lineSeparator();

        String out = runMainWithInput(input);

        assertThat(out)
                .as("Expected output to contain at least one known spacecraft from seed data")
                .containsAnyOf("Pioneer 0", "Luna 2", "Luna 3", "Ranger 7");
    }

    @Test
    @Order(6)
    void createAccount_thenCanSeeItInDatabase_andPrintsConfirmation() throws Exception {
        // Count rows before to later verify delta via direct JDBC
        int before = countAccounts();

        String input = String.join(System.lineSeparator(),
                // login first
                "AngFra",
                "MB=V4cbAqPz4vqmQ",
                "4",            // create account (menu option 4 after reordering)
                "Ada",          // first name
                "Lovelace",     // last name
                "181512-0001",  // ssn
                "s3cr3t",       // password
                "0"             // exit
        ) + System.lineSeparator();

        String out = runMainWithInput(input);

        assertThat(out)
                .containsIgnoringCase("account")
                .containsIgnoringCase("created");

        int after = countAccounts();
        assertThat(after).isEqualTo(before + 1);
    }

    @Test
    @Order(7)
    void updateAccountPassword_thenRowIsUpdated_andPrintsConfirmation() throws Exception {
        // Prepare: insert a minimal account row directly
        long userId = insertAccount("Test", "User", "111111-1111", "oldpass");

        String input = String.join(System.lineSeparator(),
                // login first
                "AngFra",
                "MB=V4cbAqPz4vqmQ",
                "5",                 // update password (menu option 5 after reordering)
                Long.toString(userId),// user_id
                "newpass123",        // new password
                "0"                  // exit
        ) + System.lineSeparator();

        String out = runMainWithInput(input);

        assertThat(out)
                .containsIgnoringCase("updated");

        String stored = readPassword(userId);
        assertThat(stored).isEqualTo("newpass123");
    }

    @Test
    @Order(8)
    void deleteAccount_thenRowIsGone_andPrintsConfirmation() throws Exception {
        long userId = insertAccount("To", "Delete", "222222-2222", "pw");

        String input = String.join(System.lineSeparator(),
                // login first
                "AngFra",
                "MB=V4cbAqPz4vqmQ",
                "6",                 // delete account (menu option 6 after reordering)
                Long.toString(userId),// user_id
                "0"                  // exit
        ) + System.lineSeparator();

        String out = runMainWithInput(input);

        assertThat(out)
                .containsIgnoringCase("deleted");

        assertThat(existsAccount(userId)).isFalse();
    }

    @Test
    @Order(4)
    void getMoonMissionById_printsDetails() throws Exception {
        // Arrange: use a known mission id from seed data (see init.sql)
        // Insert order defines auto-increment ids; 'Luna 3' is the 5th insert -> mission_id = 5
        long missionId = 5L;

        String input = String.join(System.lineSeparator(),
                // login first
                "AngFra",
                "MB=V4cbAqPz4vqmQ",
                "2",                 // menu: get mission by id (reordered to option 2)
                Long.toString(missionId),
                "0"                  // exit
        ) + System.lineSeparator();

        String out = runMainWithInput(input);

        assertThat(out)
                .as("CLI should print details that include the spacecraft name for the selected mission")
                .contains("Luna 3")
                .containsIgnoringCase("mission")
                .containsIgnoringCase("id");
    }

    @Test
    @Order(5)
    void countMoonMissionsForYear_printsTotal() throws Exception {
        int year = 2019; // Seed data contains several missions in 2019
        int expected = 3; // From init.sql: Beresheet, Chandrayaan-2, TESS

        String input = String.join(System.lineSeparator(),
                // login first
                "AngFra",
                "MB=V4cbAqPz4vqmQ",
                "3",                 // menu: count missions by year (reordered to option 3)
                Integer.toString(year),
                "0"                  // exit
        ) + System.lineSeparator();

        String out = runMainWithInput(input);

        assertThat(out)
                .as("CLI should print the number of missions for the given year")
                .contains(Integer.toString(expected))
                .containsIgnoringCase("missions")
                .contains(Integer.toString(year));
    }

    private static String runMainWithInput(String input) throws Exception {
        // Capture STDOUT
        PrintStream originalOut = System.out;
        InputStream originalIn = System.in;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(baos, true, StandardCharsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        System.setOut(capture);
        System.setIn(bais);

        try {
            // Try to find main(String[]) first, fallback to main()
            Class<?> mainClass = Class.forName("com.example.Main");
            Method method = null;
            try {
                method = mainClass.getDeclaredMethod("main", String[].class);
            } catch (NoSuchMethodException ignored) {
                try {
                    method = mainClass.getDeclaredMethod("main");
                } catch (NoSuchMethodException e) {
                    fail("Expected a main entrypoint in com.example.Main. Define either main(String[]) or main().");
                }
            }
            method.setAccessible(true);

            // Invoke with a timeout guard (in case the app blocks)
            final Method finalMethod = method;
            Thread t = new Thread(() -> {
                try {
                    if (finalMethod.getParameterCount() == 1) {
                        finalMethod.invoke(null, (Object) new String[]{});
                    } else {
                        finalMethod.invoke(null);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();
            t.join(Duration.ofSeconds(10).toMillis());
            if (t.isAlive()) {
                t.interrupt();
                fail("CLI did not exit within timeout. Ensure option '0' exits the program.");
            }

            capture.flush();
            return baos.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }
    }

    private static int countAccounts() throws SQLException {
        try (Connection c = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             PreparedStatement ps = c.prepareStatement("SELECT count(*) FROM account");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static long insertAccount(String first, String last, String ssn, String password) throws SQLException {
        try (Connection c = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO account(password, first_name, last_name, ssn) VALUES (?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, password);
            ps.setString(2, first);
            ps.setString(3, last);
            ps.setString(4, ssn);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    private static String readPassword(long userId) throws SQLException {
        try (Connection c = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             PreparedStatement ps = c.prepareStatement("SELECT password FROM account WHERE user_id = ?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getString(1);
            }
        }
    }

    private static boolean existsAccount(long userId) throws SQLException {
        try (Connection c = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM account WHERE user_id = ?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
