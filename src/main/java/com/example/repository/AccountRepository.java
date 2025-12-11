package com.example.repository;

import com.example.model.Account;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interface defining the operations for managing accounts in the repository.
 */
public interface AccountRepository {
    boolean validateLogin(String username, String password);
    long createAccount(String firstName, String lastName, String ssn, String password);
    void updatePassword(long userId, String newPassword);
    void deleteAccount(long userId);
    List<Account> listAccounts();
    Optional<Account> getById(long userId);
}