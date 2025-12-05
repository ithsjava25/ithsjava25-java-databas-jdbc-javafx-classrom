package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleDriverManagerDataSource {
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPass;

    // Resolve DB settings with precedence: System properties -> Environment variable
    public SimpleDriverManagerDataSource(String jdbcUrl, String dbUser, String dbPass){
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }
    }

    public Connection getConnection() throws SQLException{
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
    }

}
