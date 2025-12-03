package com.example.jdbc;

import com.example.repositorys.AccountRepository;

public class JdbcAccountRepository implements AccountRepository {
    @Override
    public Boolean createAccount(String firstName, String lastName, String ssn, String password) {
        return true;
    }

    @Override
    public Boolean updatePassword(int userId, String newPassword) {
        return true;
    }

    @Override
    public Boolean deleteAccount(int userId) {
        return true;
    }

    @Override
    public Boolean validateCredentials(String username, String password) {
        return true;
    }
}
