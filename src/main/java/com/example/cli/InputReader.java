package com.example.cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class InputReader {

    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public enum InputResult { CONTINUE, MENU, EXIT }

    public record InputWrapper<T>(T value, InputResult result) {}

    private String readLine() {
        try {
            String line = reader.readLine();
            return line != null ? line.trim() : "";
        } catch (Exception e) {
            throw new RuntimeException("Error reading input", e);
        }
    }

    private InputResult checkExitOrMenu(String input) {
        if (input.equals("0")) return InputResult.EXIT;
        if (input.equalsIgnoreCase("menu")) return InputResult.MENU;
        return InputResult.CONTINUE;
    }

    public InputWrapper<String> readString(String label) {
        System.out.print(label + ": ");
        System.out.flush();
        String input = readLine();
        InputResult result = checkExitOrMenu(input);
        return new InputWrapper<>(input, result);
    }

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

    public InputWrapper<String> readName(String label) {
        while (true) {
            InputWrapper<String> wrapper = readString(label);
            if (wrapper.result() != InputResult.CONTINUE) return wrapper;

            if (wrapper.value().matches("[A-Z][a-zA-Z]{2,}")) return wrapper;
            System.out.println("❌ Must start with a capital letter and be at least 3 letters ❌");
        }
    }

    public InputWrapper<String> readSSN(String label) {
        while (true) {
            InputWrapper<String> wrapper = readString(label);
            if (wrapper.result() != InputResult.CONTINUE) return wrapper;

            if (wrapper.value().matches("\\d{6}-\\d{4}")) return wrapper;
            System.out.println("❌ SSN must match format ######-#### ❌");
        }
    }

    public InputWrapper<String> readPassword(String label) {
        return readString(label);
    }
}