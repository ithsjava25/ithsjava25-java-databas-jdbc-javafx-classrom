package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountRepositoryImpl implements AccountRepository {
    Connection connection;
    PreparedStatement findAccountsStmt, createAccountStmt, countAccountsStmt;


    public AccountRepositoryImpl(String jdbc, String username, String password) {
        try {
            connection = DriverManager.getConnection(jdbc, username, password);
            findAccountsStmt = connection.prepareStatement("select * from account");
            createAccountStmt = connection.prepareStatement("insert into account(name, password, first_name, last_name, ssn) values (?, ?, ?, ?, ?)");
            countAccountsStmt = connection.prepareStatement("select count(*) from account");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> findUsernames() {
        List<String> usernames = new ArrayList<>();
        try {
            ResultSet rs = findAccountsStmt.executeQuery();
            while (rs.next()) {
                usernames.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return usernames;
    }

    @Override
    public List<String> findPasswords() {
        List<String> passwords = new ArrayList<>();
        try {
            ResultSet rs = findAccountsStmt.executeQuery();
            while (rs.next()) {
                passwords.add(rs.getString("password"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return passwords;
    }

    @Override
    public List<Account> findAccounts() {
        List<Account> accounts = new ArrayList<>();
        try {
            ResultSet rs = findAccountsStmt.executeQuery();
            while (rs.next()) {
                Account a = new Account(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("ssn")
                );
                accounts.add(a);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return accounts;
    }

    @Override
    public boolean createAccount(Account account) {
        try {
            createAccountStmt.setString(1, account.getName());
            createAccountStmt.setString(2, account.getPassword());
            createAccountStmt.setString(3, account.getFirst_name());
            createAccountStmt.setString(4, account.getLast_name());
            createAccountStmt.setString(5, account.getSsn());
            if  (createAccountStmt.executeUpdate() == 1) {
                return true;
            }


        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    @Override
    public int countAccounts() {
        int count = 0;
        try {
            ResultSet rs = countAccountsStmt.executeQuery();
            while (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }
}
