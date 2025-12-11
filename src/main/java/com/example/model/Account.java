package com.example.model;

/**
 * Represents a user account in the system.
 *
 * @param userId   unique identifier for the account
 * @param firstName first name of the account holder
 * @param lastName  last name of the account holder
 * @param ssn       social security number
 * @param password  account password
 * @param name      full display name of the account holder
 */
public record Account(
        long userId,
        String firstName,
        String lastName,
        String ssn,
        String password,
        String name
) {

    /**
     * Returns a readable string representation of the account,
     * excluding the password for security reasons.
     */
    @Override
    public String toString() {
        return "Account: " +
                "ID: " + userId +
                ", First: " + firstName +
                ", Last: " + lastName +
                ", SSN: " + ssn +
                ", Name: " + name;
    }
}