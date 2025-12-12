package com.example;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class JdbcAccountRepository implements AccountRepository {

    private final Connection connection;

    public JdbcAccountRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean createAccount(String firstName, String lastName, String ssn, String rawPassword) {
        String sql = "INSERT INTO account (first_name, last_name, name, ssn, password) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            String username = firstName + lastName;
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, username);
            pstmt.setString(4, ssn);
            pstmt.setString(5, hashedPassword);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed creating account", e);
        }
    }

    @Override
    public Account findByUsername(String username) {
        String sql = "SELECT * FROM account WHERE name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                            rs.getInt("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("ssn"),
                            rs.getString("password")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving account", e);
        }
        return null;
    }

    @Override
    public boolean verifyPassword(String username, String rawPassword) {
        Account account = findByUsername(username);

        if (account == null)
            return false;

        return BCrypt.checkpw(rawPassword, account.getPassword());
    }

    @Override
    public boolean updatePassword(int userId, String rawPassword) {
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        String sql = "UPDATE account SET password = ? WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed updating password", e);
        }
    }

    @Override
    public boolean deleteAccount(int userId) {
        String sql = "DELETE FROM account WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed deleting account", e);
        }
    }
}