package com.example;

public class JdbcAccountRepository implements AccountRepository{

    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPass;

    public JdbcAccountRepository(String jdbcUrl, String dbUser, String dbPass){
        this.jdbcUrl=jdbcUrl;
        this.dbUser=dbUser;
        this.dbPass=dbPass;
    }


    @Override
    public boolean isValidLogin(String username, String password) {
        return false;
    }

    @Override
    public boolean createAccount(String firstName, String lastName, String ssn, String password) {
        return false;
    }

    @Override
    public boolean updatePassword(long userId, String newPassword) {
        return false;
    }

    @Override
    public boolean deleteAccount(long userId) {
        return false;
    }
}
