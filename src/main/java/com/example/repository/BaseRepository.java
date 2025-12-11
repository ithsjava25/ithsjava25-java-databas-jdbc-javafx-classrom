package com.example.repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BaseRepository<T> {
    protected final DataSource dataSource;
    protected final boolean devMode;

    protected BaseRepository(DataSource dataSource, boolean devMode) {
        this.dataSource = dataSource;
        this.devMode = devMode;
        if (devMode) {
            System.out.println("[DEV] Repository " + this.getClass().getSimpleName() + " initialized");
        }
    }

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    protected void log(String msg) {
        if (devMode) System.out.println("[DEV] " + msg);
    }

    protected RepositoryException dbError(String action, Exception e) {
        log("ERROR during: " + action + " -> " + e.getMessage());
        return new RepositoryException("Database error during: " + action, e);
    }

    protected <R> R executeQuery(String sql, SQLFunction<ResultSet, R> handler, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            try (ResultSet rs = stmt.executeQuery()) {
                return handler.apply(rs);
            }
        } catch (Exception e) {
            throw dbError("executeQuery: " + sql, e);
        }
    }

    protected void executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw dbError("executeUpdate: " + sql, e);
        }
    }

    protected long executeUpdateReturnId(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw dbError("executeUpdateReturnId: " + sql, e);
        }
    }

    @FunctionalInterface
    protected interface SQLFunction<R, T> { T apply(R result) throws Exception; }

    protected abstract T map(ResultSet rs) throws SQLException;

    protected List<T> queryList(String sql, Object... params) {
        return executeQuery(sql, rs -> {
            List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }, params);
    }

    protected Optional<T> querySingle(String sql, Object... params) {
        return executeQuery(sql, rs -> {
            if (rs.next()) return Optional.of(map(rs));
            return Optional.empty();
        }, params);
    }
}