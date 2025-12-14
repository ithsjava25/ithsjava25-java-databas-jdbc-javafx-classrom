package com.example;

import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {

    private static BasicDataSource source = new BasicDataSource();
    private final static String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
    private final static String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
    private final static String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

    static {
        source.setUrl(jdbcUrl);
        source.setUsername(dbUser);
        source.setPassword(dbPass);
        source.addConnectionProperty("hibernate.hbm2ddl.auto", "update");
        source.setDefaultAutoCommit(true);
    }

    public static Connection getConnection() throws SQLException {
        return source.getConnection();
    }

    public static String getJdbcUrl(){
        return jdbcUrl;
    }

    public static String getDbUser(){
        return dbUser;
    }

    public static String getDbPass(){
        return dbPass;
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
