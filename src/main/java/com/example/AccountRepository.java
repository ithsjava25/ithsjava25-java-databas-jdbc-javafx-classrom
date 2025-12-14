package com.example;

import javax.sql.DataSource;
import java.sql.*;

public class AccountRepository {
    private final DataSource ds;

    public AccountRepository(DataSource ds) {
        this.ds = ds;
    }

    /** Kontrollera login mot name + password */
    public boolean login(String username, String password) throws SQLException {
        String sql = "SELECT 1 FROM account WHERE name=? AND password=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Skapa nytt konto och generera unikt username (name). Vid kollision läggs siffersuffix till (t.ex. AngFra1). */
    public String createAccount(String first, String last, String ssn, String password) throws SQLException {
        String base = (first == null ? "" : first.trim());
        String sur = (last == null ? "" : last.trim());
        String ssnTrim = (ssn == null ? "" : ssn.trim());

        String baseName = base.substring(0, Math.min(3, base.length()))
                + sur.substring(0, Math.min(3, sur.length()));

        String sql = "INSERT INTO account(name, password, first_name, last_name, ssn) VALUES (?,?,?,?,?)";

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(true);

            String candidate = baseName;
            int suffix = 0;
            while (true) {
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, candidate);
                    ps.setString(2, password); // medvetet: lösenord trimmas inte
                    ps.setString(3, base);
                    ps.setString(4, sur);
                    ps.setString(5, ssnTrim);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            System.out.println("Account created with user_id=" + keys.getLong(1)
                                    + " and username=" + candidate);
                        } else {
                            System.out.println("Account created with username=" + candidate);
                        }
                    }
                    return candidate;
                } catch (SQLException e) {
                    // SQLState 23000 = integrity constraint violation (includes unique constraint)
                    String sqlState = e.getSQLState();
                    if ("23000".equals(sqlState) || e.getMessage().toLowerCase().contains("duplicate") || e.getMessage().toLowerCase().contains("unique")) {
                        suffix++;
                        candidate = baseName + suffix;
                        // prova igen med nytt kandidatnamn
                        continue;
                    }
                    throw e;
                }
            }
        }
    }

    /** Uppdatera lösenord, returnerar true om lyckades */
    public boolean updatePassword(long userId, String newPassword) throws SQLException {
        String sql = "UPDATE account SET password=? WHERE user_id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setLong(2, userId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    /** Ta bort konto, returnerar true om lyckades */
    public boolean deleteAccount(long userId) throws SQLException {
        String sql = "DELETE FROM account WHERE user_id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }
}