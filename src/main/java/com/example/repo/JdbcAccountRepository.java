package com.example.repo;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class JdbcAccountRepository implements AccountRepository {

    private final DataSource ds;

    public JdbcAccountRepository(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Optional<Long> authenticate(String name, String password) {
        String sql = "SELECT user_id FROM account WHERE name = ? AND password = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getLong(1));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long createAccount(String firstName, String lastName, String ssn, String password) {
        String name = makeUsername(firstName, lastName);
        String sql = "INSERT INTO account (name, first_name, last_name, ssn, password) VALUES (?, ?, ?, ?, ?)";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, ssn);
            ps.setString(5, password);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }

            try (PreparedStatement ps2 = c.prepareStatement("SELECT user_id FROM account WHERE name = ?")) {
                ps2.setString(1, name);
                try (ResultSet rs = ps2.executeQuery()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }

            throw new IllegalStateException("Could not get new user id");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updatePassword(long userId, String newPassword) {
        String sql = "UPDATE account SET password = ? WHERE user_id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAccount(long userId) {
        String sql = "DELETE FROM account WHERE user_id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String makeUsername(String first, String last) {
        String f = first == null ? "" : first.trim();
        String l = last == null ? "" : last.trim();
        return cap(take3(f)) + cap(take3(l));
    }

    private static String take3(String s) {
        if (s.isEmpty()) return "XXX";
        return s.length() <= 3 ? s : s.substring(0, 3);
    }

    private static String cap(String s) {
        if (s.isEmpty()) return s;
        String low = s.toLowerCase();
        return Character.toUpperCase(low.charAt(0)) + low.substring(1);
    }
}
