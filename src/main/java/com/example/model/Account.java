package com.example.model;

public record Account(
        int userID,
        String name,
        String ssn,
        String password
) {
    public Account {
        if(name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        if (ssn == null || ssn.isBlank()) {
            throw new IllegalArgumentException("SSN cannot be blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
    }
}
