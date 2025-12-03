package com.example.repositorys;

public interface AccountRepository {
    Boolean createAccount(String firstName, String lastName, String ssn, String password);
    Boolean updatePassword(int userId, String newPassword);
    Boolean deleteAccount(int userId);
    Boolean validateCredentials(String username, String password);
}
