package com.example;

public interface AccountRepository {
    boolean login(String username, String password);

    long create(String first, String last, String ssn, String password);

    boolean updatePassword(int userId, String newPassword);

    boolean delete(int userId);

    boolean exists(int userId);

}
