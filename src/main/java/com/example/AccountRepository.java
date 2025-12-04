package com.example;

import java.util.List;

public interface AccountRepository {
    List<String> findUsernames();
    List<String> findPasswords();
    List<Account> findAccounts();
    boolean createAccount(Account account);
    int countAccounts();
    boolean updatePassword(int id, String password);
    boolean deleteAccount(int id);
}
