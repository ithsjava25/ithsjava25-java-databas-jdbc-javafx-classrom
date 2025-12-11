package com.example.repository;

import com.example.model.Account;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountRepositoryJdbc implements AccountRepository {
    private final DataSource ds;

    public AccountRepositoryJdbc(DataSource ds) { this.ds = ds; }

    @Override
    public Optional<Account> findNameAndPassword(String name, String password) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM account WHERE name=? AND password=?")) {
            ps.setString(1, name);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapAccount(rs));
            else return Optional.empty();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Account> findAll() {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM account")) {
            ResultSet rs = ps.executeQuery();
            List<Account> list = new ArrayList<>();
            while (rs.next()) list.add(mapAccount(rs));
            return list;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public long create(Account a) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO account(name, first_name, last_name, ssn, password) VALUES (?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.name());
            ps.setString(2, a.firstName());
            ps.setString(3, a.lastName());
            ps.setString(4, a.ssn());
            ps.setString(5, a.password());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { keys.next(); return keys.getLong(1); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean updatePassword(long id, String pw) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE account SET password=? WHERE user_id=?")) {
            ps.setString(1, pw);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean delete(long id) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM account WHERE user_id=?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        return new Account(
                rs.getLong("user_id"),
                rs.getString("name"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("ssn"),
                rs.getString("password")
        );
    }
}
