package com.example.cli;

public class MenuCLI {

    private final AccountCLI accountCLI;
    private final MoonMissionCLI missionCLI;
    private final InputReader input;

    public MenuCLI(AccountCLI accountCLI, MoonMissionCLI missionCLI, InputReader input) {
        this.accountCLI = accountCLI;
        this.missionCLI = missionCLI;
        this.input = input;
    }

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
