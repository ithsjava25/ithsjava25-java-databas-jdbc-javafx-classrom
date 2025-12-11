package com.example.cli;

/**
 * Handles the main menu display and routes user selections
 * to the appropriate CLI handlers for accounts or moon missions.
 */
public class MenuCLI {

    private final AccountCLI accountCLI;
    private final MoonMissionCLI missionCLI;
    private final InputReader input;

    /**
     * Creates a MenuCLI with the required CLI handlers and input reader.
     *
     * @param accountCLI CLI handler for account-related actions
     * @param missionCLI CLI handler for moon mission-related actions
     * @param input input reader for user interaction
     */
    public MenuCLI(AccountCLI accountCLI, MoonMissionCLI missionCLI, InputReader input) {
        this.accountCLI = accountCLI;
        this.missionCLI = missionCLI;
        this.input = input;
    }

    /**
     * Displays the main menu, handles user input, and routes commands
     * to the appropriate CLI methods until the user exits.
     */
    public void showMainMenu() {
        while (true) {
            printHeader();
            var choiceWrapper = input.readInt("Choose option");

            if (choiceWrapper.result() == InputReader.InputResult.EXIT) {
                System.out.println("Exiting...");
                return;
            }

            if (choiceWrapper.result() == InputReader.InputResult.MENU) continue;

            switch (choiceWrapper.value()) {
                case 1 -> missionCLI.listMissions();
                case 2 -> missionCLI.getMissionById();
                case 3 -> missionCLI.countMissionsByYear();
                case 4 -> accountCLI.createAccount();
                case 5 -> accountCLI.updatePassword();
                case 6 -> accountCLI.deleteAccount();
                default -> System.out.println("Invalid option, try again.");
            }
        }
    }

    /** Prints the menu header and available options. */
    private void printHeader() {
        System.out.println("\n         🌕 MOON MISSION HUB 🌕         ");
        System.out.println("----------------------------------------");
        System.out.println("Type 0 to exit from this menu, or 'menu' to go back to the main menu.");
        System.out.println("  1) List moon missions");
        System.out.println("  2) Get mission by ID");
        System.out.println("  3) Count missions by year");
        System.out.println("  4) Create account");
        System.out.println("  5) Update password");
        System.out.println("  6) Delete account");
        System.out.println("  0) Exit\n");
    }
}
