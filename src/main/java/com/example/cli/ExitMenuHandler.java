package com.example.cli;

/**
 * Provides a default method to handle 'exit' or 'menu' commands from the user.
 * Classes implementing this interface can easily check if the user wants to exit or return to the menu.
 */
public interface ExitMenuHandler {

    /**
     * Handles input results indicating exit or menu commands.
     *
     * @param result the result from InputReader (CONTINUE, EXIT, MENU)
     * @return true if the user wants to exit, false if continue or menu
     */
    default boolean handleExitOrMenu(InputReader.InputResult result) {
        if (result == InputReader.InputResult.EXIT) {
            System.out.println("Exiting...");
            return true;
        }
        return result == InputReader.InputResult.MENU;
    }
}