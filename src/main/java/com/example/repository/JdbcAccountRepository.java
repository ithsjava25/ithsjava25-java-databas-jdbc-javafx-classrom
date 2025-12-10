package com.example.repository;

import com.example.model.Account;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class JdbcAccountRepository implements AccountRepository {

    private final DataSource dataSource;

    public JdbcAccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Account> findByUsernameAndPassword(String username, String password) {
        String query = "SELECT user_id, name, ssn, password FROM account WHERE name = ? AND password = ?";

        try(Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    Account account = new Account(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("ssn"),
                            rs.getString("password")
                    );
                    return Optional.of(account);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during login", e);
        }
        return Optional.empty();
    }

    @Override
    public int create(String firstName, String lastName, String ssn, String password) {

        String username = firstName + lastName;
        String query = "INSERT INTO account (name, ssn, password) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, ssn);
            stmt.setString(3, password);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating account", e);
        }
        return -1;
    }

    @Override
    public boolean updatePassword(int userId, String newPassword) {
        String query = "UPDATE account SET password = ? WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error updating password", e);
        }
    }

    @Override
    public boolean delete(int userId) {
        String query = "DELETE FROM account WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting account", e);
        }
    }
}
