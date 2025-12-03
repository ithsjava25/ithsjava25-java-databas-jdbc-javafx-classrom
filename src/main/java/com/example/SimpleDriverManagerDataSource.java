package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleDriverManagerDataSource {
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPass;

    // Resolve DB settings with precedence: System properties -> Environment variable
    public SimpleDriverManagerDataSource(){
        this.jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        this.dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        this.dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");
        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }
    }

    public Connection getConnection() throws SQLException{
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
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
