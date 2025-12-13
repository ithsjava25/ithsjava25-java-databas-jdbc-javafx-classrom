package com.example;

public class Account {

    private int userId;
    private String firstName;
    private String lastName;
    private String ssn;
    private String password;



    public Account(int userId, String firstName, String lastName, String ssn, String password, String string) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
        this.password = password;
    }



    public String getPassword() {
        return password;
    }
}