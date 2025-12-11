package com.example.service;

import com.example.model.Account;
import com.example.repository.AccountRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AccountService {
    private final AccountRepository repo;

    public AccountService(AccountRepository repo) { this.repo = repo; }

    public boolean validateLogin(String username, String password) throws SQLException {
        return repo.validateLogin(username, password);
    }

    public long createAccount(String firstName, String lastName, String ssn, String password) throws SQLException {
        return repo.createAccount(firstName, lastName, ssn, password);
    }

    public void updatePassword(long userId, String newPassword) throws SQLException {
        repo.updatePassword(userId, newPassword);
    }

    public void deleteAccount(long userId) throws SQLException {
        repo.deleteAccount(userId);
    }

    public List<Account> listAccounts() throws SQLException { return repo.listAccounts(); }

    public Optional<Account> getById(long userId) throws SQLException { return repo.getById(userId); }
}
