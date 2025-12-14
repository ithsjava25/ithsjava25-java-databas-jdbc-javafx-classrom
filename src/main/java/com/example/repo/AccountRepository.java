package com.example.repo;

import java.util.Optional;

public interface AccountRepository {
    Optional<Long> authenticate(String name, String password);
    long createAccount(String firstName, String lastName, String ssn, String password);
    void updatePassword(long userId, String newPassword);
    void deleteAccount(long userId);
}
