package com.example;

import com.example.cli.*;
import com.example.repository.*;
import com.example.service.*;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException {
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

        }
    }
}
