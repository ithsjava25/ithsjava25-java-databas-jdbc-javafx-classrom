package com.example.repository;

import com.example.model.Account;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    boolean validateLogin(String username, String password) throws SQLException;
    long createAccount(String firstName, String lastName, String ssn, String password) throws SQLException;
    void updatePassword(long userId, String newPassword) throws SQLException;
    void deleteAccount(long userId) throws SQLException;
    List<Account> listAccounts() throws SQLException;
    Optional<Account> getById(long userId) throws SQLException;
}
