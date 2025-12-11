package com.example.cli;

import java.io.IOException;
import java.sql.SQLException;

public class MenuCLI {


    // MENU LOOP
    boolean exit = false;
            while (!exit) {
        System.out.println("\nMenu:");
        System.out.println("1) List moon missions");
        System.out.println("2) Get a moon mission by mission_id");
        System.out.println("3) Count missions for a given year");
        System.out.println("4) Create an account");
        System.out.println("5) Update an account password");
        System.out.println("6) Delete an account");
        System.out.println("0) Exit");
        System.out.print("Choose an option: ");
        String choice = readLine(in);

        switch (choice) {
            case "1":
                listMoonMissions(connection);
                break;
            case "2":
                getMoonMissionById(connection, in);
                break;
            case "3":
                countMissionsByYear(connection, in);
                break;
            case "4":
                createAccount(connection, in);
                break;
            case "5":
                updateAccountPassword(connection, in);
                break;
            case "6":
                deleteAccount(connection, in);
                break;
            case "0":
                exit = true;
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

} catch (SQLException | IOException e) {
        throw new RuntimeException(e);
        }
                }

                }
