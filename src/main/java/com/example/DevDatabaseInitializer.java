package com.example;


import org.testcontainers.containers.MySQLContainer;

/**
 * Initializes a MySQL development database using Testcontainers.
 * Sets system properties for JDBC URL, username, and password after startup.
 */
public class DevDatabaseInitializer {
    private static MySQLContainer<?> mysql;

    public static void start() {
        if (mysql == null) {
            mysql = new MySQLContainer<>("mysql:9.5.0")
                    .withDatabaseName("testdb")
                    .withUsername("user")
                    .withPassword("password")
                    .withConfigurationOverride("myconfig")
                    .withInitScript("init.sql");
            mysql.start();

            System.setProperty("APP_JDBC_URL", mysql.getJdbcUrl());
            System.setProperty("APP_DB_USER", mysql.getUsername());
            System.setProperty("APP_DB_PASS", mysql.getPassword());
        }
    }
}