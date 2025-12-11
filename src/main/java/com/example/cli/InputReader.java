package com.example.cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Handles user input from the console with validation and special commands.
 * Supports reading strings, integers, user IDs, names, SSNs, and passwords.
 */
public class InputReader {

    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Represents the result of reading input.
     * CONTINUE = normal input, MENU = user wants to go back, EXIT = user wants to exit.
     */
    public enum InputResult { CONTINUE, MENU, EXIT }

    /**
     * Wraps a value read from the user along with the input result status.
     *
     * @param <T> the type of the input value
     * @param value the actual input value
     * @param result the input result (CONTINUE, MENU, EXIT)
     */
    public record InputWrapper<T>(T value, InputResult result) {}

    // Reads a line from the console
    private String readLine() {
        try {
            String line = reader.readLine();
            return line != null ? line.trim() : "";
        } catch (Exception e) {
            throw new RuntimeException("Error reading input", e);
        }
    }

    // Checks if input is '0' (exit) or 'menu'
    private InputResult checkExitOrMenu(String input) {
        if (input.equals("0")) return InputResult.EXIT;
        if (input.equalsIgnoreCase("menu")) return InputResult.MENU;
        return InputResult.CONTINUE;
    }

    /** Reads a string from the user with exit/menu handling. */
    public InputWrapper<String> readString(String label) {
        System.out.print(label + ": ");
        System.out.flush();
        String input = readLine();
        InputResult result = checkExitOrMenu(input);
        return new InputWrapper<>(input, result);
    }

    /** Reads an integer from the user with validation and exit/menu handling. */
    public InputWrapper<Integer> readInt(String label) {
        while (true) {
            InputWrapper<String> wrapper = readString(label);
            if (wrapper.result() != InputResult.CONTINUE) return new InputWrapper<>(0, wrapper.result());

            try {
                return new InputWrapper<>(Integer.parseInt(wrapper.value()), InputResult.CONTINUE);
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid number ❌");
            }
        }
    }

    /** Reads a valid user ID (long) from the user with validation and exit/menu handling. */
    public InputWrapper<Long> readValidUserId(String label) {
        while (true) {
            InputWrapper<String> wrapper = readString(label);
            if (wrapper.result() != InputResult.CONTINUE) return new InputWrapper<>(0L, wrapper.result());

            try {
                return new InputWrapper<>(Long.parseLong(wrapper.value()), InputResult.CONTINUE);
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid number ❌");
            }
        }
    }

    /** Reads a valid name from the user (capitalized, at least 3 letters). */
    public InputWrapper<String> readName(String label) {
        while (true) {
            InputWrapper<String> wrapper = readString(label);
            if (wrapper.result() != InputResult.CONTINUE) return wrapper;

            if (wrapper.value().matches("[A-Z][a-zA-Z]{2,}")) return wrapper;
            System.out.println("❌ Must start with a capital letter and be at least 3 letters ❌");
        }
    }

    /** Reads a valid Swedish SSN from the user (######-####). */
    public InputWrapper<String> readSSN(String label) {
        while (true) {
            InputWrapper<String> wrapper = readString(label);
            if (wrapper.result() != InputResult.CONTINUE) return wrapper;

            if (wrapper.value().matches("\\d{6}-\\d{4}")) return wrapper;
            System.out.println("❌ SSN must match format ######-#### ❌");
        }
    }

    /** Reads a password from the user. */
    public InputWrapper<String> readPassword(String label) {
        return readString(label);
    }
}