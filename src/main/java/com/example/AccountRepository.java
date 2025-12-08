package com.example;

public interface AccountRepository {

    boolean isValidLogin(String username, String password);

    boolean createAccount(String firstName, String lastName, String ssn, String password);

    boolean updatePassword(long userId, String newPassword);

    boolean deleteAccount(long userId);
}
