package com.example.repository;

import com.example.model.Account;

import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findByUsernameAndPassword (String username, String password);

    int create (String firstName, String lastName, String ssn, String password);

    boolean updatePassword(int userId, String newPassword);

    boolean delete(int userId);
}
