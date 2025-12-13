package com.example;

import com.example.AccountRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcAccountRepository implements AccountRepository {

    private final Connection connection;

    public JdbcAccountRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean createAccount(String firstName, String lastName, String ssn, String password) {
        String sql = """
            INSERT INTO account (first_name, last_name, name, ssn, password)
            VALUES (?, ?, ?, ?, ?)
            """;

        String username = firstName.substring(0, 3) + lastName.substring(0, 3);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, username);
            ps.setString(4, ssn);
            ps.setString(5, password); // plain text

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean updatePassword(int userId, String password) {
        String sql = "UPDATE account SET password = ? WHERE user_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, password);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean deleteAccount(int userId) {
        String sql = "DELETE FROM account WHERE user_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
