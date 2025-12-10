package com.example.datasource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class SimpleDriverManagerDataSource implements DataSource {

    private final String url;
    private final String username;
    private final String password;

    public SimpleDriverManagerDataSource(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public java.io.PrintWriter getLogWriter() { return null; }

    @Override
    public void setLogWriter(java.io.PrintWriter out) {}

    @Override
    public void setLoginTimeout(int seconds) {}

    @Override
    public int getLoginTimeout() { return 0; }

    @Override
    public Logger getParentLogger() { return null; }

    @Override
    public <T> T unwrap(Class<T> iface) { return null; }

    @Override
    public boolean isWrapperFor(Class<?> iface) { return false; }
}
