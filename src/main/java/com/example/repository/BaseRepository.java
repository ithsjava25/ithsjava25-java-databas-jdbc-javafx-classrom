package com.example.repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Abstract base class for JDBC repositories.
 * Provides common database operations such as query, update, and mapping.
 *
 * @param <T> the type of entity this repository handles
 */
public abstract class BaseRepository<T> {
    protected final DataSource dataSource;
    protected final boolean devMode;

    /**
     * Constructs the repository with a DataSource.
     *
     * @param dataSource the database source
     * @param devMode    enables debug logging if true
     */
    protected BaseRepository(DataSource dataSource, boolean devMode) {
        this.dataSource = dataSource;
        this.devMode = devMode;
        if (devMode) {
            System.out.println("[DEV] Repository " + this.getClass().getSimpleName() + " initialized");
        }
    }

    /**
     * Gets a database connection from the DataSource.
     */
    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Logs messages if devMode is enabled.
     */
    protected void log(String msg) {
        if (devMode) System.out.println("[DEV] " + msg);
    }

    /**
     * Wraps database exceptions into a RepositoryException.
     */
    protected RepositoryException dbError(String action, Exception e) {
        log("ERROR during: " + action + " -> " + e.getMessage());
        return new RepositoryException("Database error during: " + action, e);
    }

    /**
     * Executes a query and applies a handler function to the ResultSet.
     */
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

    /**
     * Executes an update/insert/delete SQL statement.
     */
    protected void executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw dbError("executeUpdate: " + sql, e);
        }
    }

    /**
     * Executes an insert and returns the generated key.
     */
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

    /**
     * Functional interface for processing ResultSets.
     */
    @FunctionalInterface
    protected interface SQLFunction<R, T> { T apply(R result) throws Exception; }

    /**
     * Maps a ResultSet row to an entity.
     *
     * @param rs the ResultSet row
     * @return the mapped entity
     */
    protected abstract T map(ResultSet rs) throws SQLException;

    /**
     * Executes a query and returns a list of entities.
     */
    protected List<T> queryList(String sql, Object... params) {
        return executeQuery(sql, rs -> {
            List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }, params);
    }

    /**
     * Executes a query and returns a single entity wrapped in Optional.
     */
    protected Optional<T> querySingle(String sql, Object... params) {
        return executeQuery(sql, rs -> {
            if (rs.next()) return Optional.of(map(rs));
            return Optional.empty();
        }, params);
    }
}