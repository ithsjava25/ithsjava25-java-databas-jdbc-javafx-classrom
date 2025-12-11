package com.example.cli;

public interface ExitMenuHandler {

    default boolean handleExitOrMenu(InputReader.InputResult result) {
        if (result == InputReader.InputResult.EXIT) {
            System.out.println("Exiting...");
            return true;
        }
        return result == InputReader.InputResult.MENU;
    }
}