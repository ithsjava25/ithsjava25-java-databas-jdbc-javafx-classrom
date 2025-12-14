package com.example;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class SimpleDriverManagerDataSource implements DataSource {
    private final String url;
    private final String user;
    private final String pass;

    public SimpleDriverManagerDataSource(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public PrintWriter getLogWriter() {
        throw new UnsupportedOperationException("getLogWriter not supported");
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        throw new UnsupportedOperationException("setLogWriter not supported");
    }

    @Override
    public void setLoginTimeout(int seconds) {
        throw new UnsupportedOperationException("setLoginTimeout not supported");
    }

    @Override
    public int getLoginTimeout() {
        throw new UnsupportedOperationException("getLoginTimeout not supported");
    }

    @Override
    public Logger getParentLogger() {
        throw new UnsupportedOperationException("getParentLogger not supported");
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        throw new UnsupportedOperationException("unwrap not supported");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }
}