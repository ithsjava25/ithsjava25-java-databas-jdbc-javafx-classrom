package com.example.cli;

import com.example.model.Account;
import com.example.service.AccountService;
import java.sql.SQLException;
import java.util.List;

public class AccountCLI implements ExitMenuHandler {

    private final AccountService service;
    private final InputReader input;

    public AccountCLI(AccountService service, InputReader input) {
        this.service = service;
        this.input = input;
    }

    public void createAccount() {
        try {
            var first = input.readName("First name");
            if (handleExitOrMenu(first.result())) return;

            var last = input.readName("Last name");
            if (handleExitOrMenu(last.result())) return;

            var ssn = input.readSSN("SSN");
            if (handleExitOrMenu(ssn.result())) return;

            var pass = input.readPassword("Password");
            if (handleExitOrMenu(pass.result())) return;

            long id = service.createAccount(first.value(), last.value(), ssn.value(), pass.value());
            System.out.println("\n✅ Account created with ID: " + id + " ✅\n");
        } catch (SQLException e) {
            System.out.println("❌ Error creating account: " + e.getMessage());
        }
    }

    public void updatePassword() {
        try {
            List<Account> accounts = service.listAccounts();
            if (accounts.isEmpty()) {
                System.out.println("❌ No accounts found ❌");
                return;
            }

            System.out.println("\n📋 Existing accounts:");
            for (Account acc : accounts) {
                System.out.println(acc);  // toString() används automatiskt
            }

            var idWrapper = input.readValidUserId("User ID");
            if (handleExitOrMenu(idWrapper.result())) return;

            if (service.getById(idWrapper.value()).isEmpty()) {
                System.out.println("❌ Account with this ID does not exist ❌");
                return;
            }

            var passWrapper = input.readPassword("New Password");
            if (handleExitOrMenu(passWrapper.result())) return;

            service.updatePassword(idWrapper.value(), passWrapper.value());
            System.out.println("\n✅ Password updated ✅\n");

        } catch (SQLException e) {
            System.out.println("❌ Error updating password: " + e.getMessage());
        }
    }




    public void deleteAccount() {
        try {
            List<Account> accounts = service.listAccounts();
            if (accounts.isEmpty()) {
                System.out.println("❌ No accounts found ❌");
                return;
            }

            System.out.println("\n📋 Existing accounts:");
            for (Account acc : accounts) {
                System.out.println(acc);
            }

            var idWrapper = input.readValidUserId("User ID");
            if (handleExitOrMenu(idWrapper.result())) return;

            if (service.getById(idWrapper.value()).isEmpty()) {
                System.out.println("❌ Account with this ID does not exist ❌");
                return;
            }

            while (true) {
                var confirmWrapper = input.readString("Are you sure you want to delete this account? (yes/no)");
                if (handleExitOrMenu(confirmWrapper.result())) return;

                String confirm = confirmWrapper.value();
                if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
                    service.deleteAccount(idWrapper.value());
                    System.out.println("\n✅ Account deleted ✅\n");
                    break;
                } else if (confirm.equalsIgnoreCase("no") || confirm.equalsIgnoreCase("n")) {
                    System.out.println("❌ Account deletion cancelled ❌\n");
                    break;
                } else {
                    System.out.println("❌ Invalid input, type yes, no, or menu ❌");
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error deleting account: " + e.getMessage());
        }
    }
}