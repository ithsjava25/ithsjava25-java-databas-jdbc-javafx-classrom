package com.example;




public class Account {

    private int userId;
    private String firstName;
    private String lastName;
    private String ssn;
    private String password;

    public Account(int userId, String firstName, String lastName, String ssn, String password) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
        this.password = password;
    }




    public String getFirstName() {

        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public String getSsn() {

        return this.ssn;
    }

    public int getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;

    }

    public void setHashedPassword(String password) {
        this.password = password;

    }
}