package com.example;

import com.example.Account;
import com.example.AccountRepository;
import com.example.AccountRepositoryJdbc;
import com.example.MoonMissionRepository;
import com.example.MoonMissionRepositoryJdbc;

import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() {
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException("Missing DB configuration...");
        }

        SimpleDriverManagerDataSource ds = new SimpleDriverManagerDataSource(jdbcUrl, dbUser, dbPass);
        AccountRepository accountRepo = new AccountRepositoryJdbc(ds);
        MoonMissionRepository missionRepo = new MoonMissionRepositoryJdbc(ds);

        Scanner sc = new Scanner(System.in);

        System.out.println("Username:");
        String username = sc.nextLine();
        System.out.println("Password:");
        String password = sc.nextLine();

        if (accountRepo.findByNameAndPassword(username, password).isPresent()) {
            System.out.println("username accepted");
            menuLoop(accountRepo, missionRepo, sc);
        } else {
            System.out.println("Invalid username or password");
            System.out.println("0) Exit");
            String opt = sc.nextLine();
            if ("0".equals(opt)) {
                return;
            }
        }
    }

    private void menuLoop(AccountRepository accountRepo, MoonMissionRepository missionRepo, Scanner sc) {
        boolean running = true;
        while (running) {
            System.out.println("Menu:");
            System.out.println("1) List moon missions");
            System.out.println("2) Get mission by id");
            System.out.println("3) Count missions by year");
            System.out.println("4) Create account");
            System.out.println("5) Update account password");
            System.out.println("6) Delete account");
            System.out.println("0) Exit");

            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    missionRepo.findAll().forEach(m -> System.out.println(m.spacecraft()));
                    break;
                case "2":
                    System.out.println("mission_id:");
                    int id = Integer.parseInt(sc.nextLine());
                    missionRepo.findById(id).ifPresentOrElse(
                            m -> System.out.println("Mission " + m.missionId() + ": " + m.spacecraft()),
                            () -> System.out.println("No mission found")
                    );
                    break;
                case "3":
                    System.out.println("year:");
                    int year = Integer.parseInt(sc.nextLine());
                    int count = missionRepo.countByYear(year);
                    System.out.println(count + " missions in " + year);
                    break;
                case "4":
                    System.out.println("first name:");
                    String fn = sc.nextLine();
                    System.out.println("last name:");
                    String ln = sc.nextLine();
                    System.out.println("ssn:");
                    String ssn = sc.nextLine();
                    System.out.println("password:");
                    String pw = sc.nextLine();
                    String name = fn.substring(0,3) + ln.substring(0,3);
                    accountRepo.create(new Account(0, name, pw, fn, ln, ssn));
                    System.out.println("account created");
                    break;
                case "5":
                    System.out.println("user_id:");
                    int uid = Integer.parseInt(sc.nextLine());
                    System.out.println("new password:");
                    String newPw = sc.nextLine();
                    if (accountRepo.updatePassword(uid, newPw)) {
                        System.out.println("updated");
                    }
                    break;
                case "6":
                    System.out.println("user_id:");
                    int delId = Integer.parseInt(sc.nextLine());
                    if (accountRepo.delete(delId)) {
                        System.out.println("deleted");
                    }
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode")) return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE"))) return true;
        return Arrays.asList(args).contains("--dev");
    }

    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}
