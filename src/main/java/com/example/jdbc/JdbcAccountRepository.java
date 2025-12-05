package com.example.jdbc;

import com.example.repositorys.AccountRepository;
import com.example.SimpleDriverManagerDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcAccountRepository implements AccountRepository {

    private final SimpleDriverManagerDataSource ds;

    public JdbcAccountRepository(SimpleDriverManagerDataSource ds) {
        this.ds = ds;
    }

    @Override
    public Boolean createAccount(String firstName, String lastName, String ssn, String password) {
        String sql = "INSERT INTO account (first_name, last_name, ssn, password) VALUES (?, ?, ?, ?)";
        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, ssn);
            ps.setString(4, password);

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating account", e);
        }
    }

    @Override
    public Boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE account SET password = ? WHERE user_id = ?";
        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setInt(2, userId);

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating password", e);
        }
    }

    @Override
    public Boolean deleteAccount(int userId) {
        String sql = "DELETE FROM account WHERE user_id = ?";
        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting account", e);
        }
    }

    @Override
    public Boolean validateCredentials(String username, String password) {
        // Adjust column names depending on your schema!
        String sql = "SELECT * FROM account WHERE name = ? AND password = ?";
        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
