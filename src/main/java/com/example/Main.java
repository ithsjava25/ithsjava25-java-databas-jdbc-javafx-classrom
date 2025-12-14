package com.example;

import com.example.db.SimpleDriverManagerDataSource;
import com.example.repo.*;

import javax.sql.DataSource;
import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }

        try {
            new Main().run(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void run(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out), true);

        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException("Missing database configuration");
        }

        DataSource ds = new SimpleDriverManagerDataSource(jdbcUrl, dbUser, dbPass);
        AccountRepository accounts = new JdbcAccountRepository(ds);
        MoonMissionRepository missions = new JdbcMoonMissionRepository(ds);

        while (true) {
            out.print("Username:");
            out.flush();
            String username = in.readLine();
            if (username == null) return;
            if ("0".equals(username.trim())) return;

            out.print("Password:");
            out.flush();
            String password = in.readLine();
            if (password == null) return;

            if (accounts.authenticate(username, password).isPresent()) {
                break;
            }

            out.println("Invalid username or password");
            out.println("0) Exit");
        }

        while (true) {
            out.println("1) List moon missions");
            out.println("2) Get a moon mission by mission_id");
            out.println("3) Count missions for a given year");
            out.println("4) Create an account");
            out.println("5) Update an account password");
            out.println("6) Delete an account");
            out.println("0) Exit");

            String choice = in.readLine();
            if (choice == null) return;

            switch (choice.trim()) {
                case "1" -> {
                    for (String name : missions.listSpacecraftNames()) {
                        out.println(name);
                    }
                }
                case "2" -> {
                    out.print("mission_id:");
                    out.flush();
                    long id = Long.parseLong(in.readLine());
                    missions.getMissionAsMap(id)
                            .ifPresentOrElse(
                                    m -> out.println(format(m)),
                                    () -> out.println("not found")
                            );
                }
                case "3" -> {
                    out.print("year:");
                    out.flush();
                    int year = Integer.parseInt(in.readLine());
                    int count = missions.countByYear(year);
                    out.println(year + ": " + count);
                }
                case "4" -> {
                    out.print("first name:");
                    out.flush();
                    String first = in.readLine();

                    out.print("last name:");
                    out.flush();
                    String last = in.readLine();

                    out.print("ssn:");
                    out.flush();
                    String ssn = in.readLine();

                    out.print("password:");
                    out.flush();
                    String pw = in.readLine();

                    long id = accounts.createAccount(first, last, ssn, pw);
                    out.println("account created " + id);
                }
                case "5" -> {
                    out.print("user_id:");
                    out.flush();
                    long id = Long.parseLong(in.readLine());

                    out.print("new password:");
                    out.flush();
                    String pw = in.readLine();

                    accounts.updatePassword(id, pw);
                    out.println("password updated");
                }
                case "6" -> {
                    out.print("user_id:");
                    out.flush();
                    long id = Long.parseLong(in.readLine());

                    accounts.deleteAccount(id);
                    out.println("account deleted");
                }
                case "0" -> {
                    return;
                }
                default -> out.println("invalid option");
            }
        }
    }

    private static String format(Map<String, Object> row) {
        return row.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + " " + b)
                .orElse("");
    }

    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode")) return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE"))) return true;
        return args != null && Arrays.asList(args).contains("--dev");
    }

    private static String resolveConfig(String prop, String env) {
        String v = System.getProperty(prop);
        if (v == null || v.isBlank()) v = System.getenv(env);
        return (v == null || v.isBlank()) ? null : v.trim();
    }
}
