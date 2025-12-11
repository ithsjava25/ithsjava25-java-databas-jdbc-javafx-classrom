package com.example;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple DataSource implementation using DriverManager.
 *
 * <p>
 * Provides basic JDBC connections using a URL, username, and password.
 * Only getConnection methods are supported; other DataSource features throw
 * UnsupportedOperationException.
 * </p>
 */
public class SimpleDriverManagerDataSource implements DataSource {
    private final String url;
    private final String username;
    private final String password;

    /**
     * Creates a new DataSource with the given JDBC parameters.
     *
     * @param url the JDBC URL
     * @param username the database username
     * @param password the database password
     */
    public SimpleDriverManagerDataSource(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException { return DriverManager.getConnection(url, username, password); }

    @Override
    public Connection getConnection(String username, String password) throws SQLException { return DriverManager.getConnection(url, username, password); }

    @Override public <T> T unwrap(Class<T> iface) { throw new UnsupportedOperationException(); }
    @Override public boolean isWrapperFor(Class<?> iface) { return false; }
    @Override public java.io.PrintWriter getLogWriter() { throw new UnsupportedOperationException(); }
    @Override public void setLogWriter(java.io.PrintWriter out) { throw new UnsupportedOperationException(); }
    @Override public void setLoginTimeout(int seconds) { throw new UnsupportedOperationException(); }
    @Override public int getLoginTimeout() { return 0; }
    @Override public java.util.logging.Logger getParentLogger() { throw new UnsupportedOperationException(); }
}