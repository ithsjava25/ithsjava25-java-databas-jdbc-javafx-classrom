package com.example.cli;

import com.example.service.AccountService;
import com.example.repository.RepositoryException;

/**
 * Handles user login attempts, including input reading, validation, and retry limits.
 */
public class LoginManager implements ExitMenuHandler {

    private final AccountService service;
    private final int maxAttempts;
    private final InputReader input;

    /**
     * Creates a LoginManager with default max attempts (5).
     */
    public LoginManager(AccountService service, InputReader input) {
        this(service, input, 5);
    }

    /**
     * Creates a LoginManager with a specified maximum number of login attempts.
     *
     * @param maxAttempts maximum allowed attempts before login fails
     */
    public LoginManager(AccountService service, InputReader input, int maxAttempts) {
        this.service = service;
        this.input = input;
        this.maxAttempts = maxAttempts;
    }

    /**
     * Performs the login process, asking for username and password.
     * Returns true if login succeeds, false if attempts are exhausted or user exits.
     */
    public boolean login() {
        System.out.println("Type 0 to exit anytime.");

        int attempts = 0;
        while (attempts < maxAttempts) {
            var usernameWrapper = input.readString("Username");
            if (handleExitOrMenu(usernameWrapper.result())) return false;

            var passwordWrapper = input.readPassword("Password");
            if (handleExitOrMenu(passwordWrapper.result())) return false;

            try {
                if (service.validateLogin(usernameWrapper.value(), passwordWrapper.value())) {
                    System.out.println("\n✅ Login successful! Welcome, " + usernameWrapper.value() + "!\n");
                    return true;
                } else {
                    System.out.println("❌ Invalid username or password ❌");
                }
            } catch (RepositoryException e) {
                System.out.println("❌ Error validating login: " + e.getMessage());
            }

            attempts++;
            if (attempts < maxAttempts) {
                System.out.println("Attempts left: " + (maxAttempts - attempts));
            }
        }
        return false;
    }
}
