package com.example;

public interface AccountRepository {
    boolean createAccount(String firstName, String lastName, String ssn, String password);
    boolean deleteAccount(int userId);
    Account findByUsername(String username);
    boolean verifyPassword(String username, String rawPassword);
    boolean updatePassword(int userId, String hashedPassword);


}
