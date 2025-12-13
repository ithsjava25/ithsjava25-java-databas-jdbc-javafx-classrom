package com.example;

import java.sql.*;

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

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, firstName + lastName); // username
            ps.setString(4, ssn);
            ps.setString(5, password); // PLAIN TEXT (required by tests)
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Account findByUsername(String username) {
        String sql = "SELECT * FROM account WHERE name = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Account(
                        rs.getInt("user_id"),
                        rs.getString("first_name"),
                        rs.getString("name"),
                        rs.getString("last_name"),
                        rs.getString("ssn"),
                        rs.getString("password")

                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public boolean verifyPassword(String username, String rawPassword) {
        Account acc = findByUsername(username);
        return acc != null && acc.getPassword().equals(rawPassword);
    }

    @Override
    public boolean updatePassword(int userId, String password) {
        String sql = "UPDATE account SET password = ? WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, password);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean deleteAccount(int userId) {
        String sql = "DELETE FROM account WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
    }
}