package com.example;

import com.example.cli.*;
import com.example.repository.*;
import com.example.service.*;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        if (ConfigUtils.isDevMode(args)) {
            DevDatabaseInitializer.start();
        }

        String jdbcUrl = ConfigUtils.resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = ConfigUtils.resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = ConfigUtils.resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException("Missing DB configuration.");
        }

        SimpleDriverManagerDataSource dataSource = new SimpleDriverManagerDataSource(jdbcUrl, dbUser, dbPass);
        boolean devMode = ConfigUtils.isDevMode(args);

        AccountRepositoryJdbc accountRepo = new AccountRepositoryJdbc(dataSource, devMode);
        MoonMissionRepositoryJdbc missionRepo = new MoonMissionRepositoryJdbc(dataSource, devMode);

        AccountService accountService = new AccountService(accountRepo);
        MoonMissionService missionService = new MoonMissionService(missionRepo);

        InputReader input = new InputReader();

        AccountCLI accountCLI = new AccountCLI(accountService, input);
        MoonMissionCLI missionCLI = new MoonMissionCLI(missionService, input);
        MenuCLI menu = new MenuCLI(accountCLI, missionCLI, input);

        LoginManager loginManager = new LoginManager(accountService, input);

        if (loginManager.login()) {
            menu.showMainMenu();
        }
    }
}