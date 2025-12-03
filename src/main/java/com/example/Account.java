package com.example;

public class Account {
    private int user_id;
    private String name, password, first_name, last_name, ssn;

    public Account(int user_id, String name, String password, String first_name, String last_name, String ssn) {
        this.user_id = user_id;
        this.name = name;
        this.password = password;
        this.first_name = first_name;
        this.last_name = last_name;
        this.ssn = ssn;
    }

    public Account(String name, String password, String first_name, String last_name, String ssn) {
        this.name = name;
        this.password = password;
        this.first_name = first_name;
        this.last_name = last_name;
        this.ssn = ssn;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    @Override
    public String toString() {
        return "Account{" +
                "user_id=" + user_id +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", ssn='" + ssn + '\'' +
                '}';
    }
}
