package com.example;

public class Account {

    private final int userId;
    private final String firstName;
    private final String lastName;
    private final String username;
    private final String ssn;
    private final String password;

    public Account(int userId, String firstName, String lastName,
                   String username, String ssn, String password) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.ssn = ssn;
        this.password = password;
    }



    public int getUserId() {
        return userId; }
    public String getFirstName() {
        return firstName; }
    public String getLastName() {
        return lastName; }
    public String getUsername() {
        return username;
    }
    public String getSsn() {
        return ssn; }
    public String getPassword() {
        return password; }
    }
