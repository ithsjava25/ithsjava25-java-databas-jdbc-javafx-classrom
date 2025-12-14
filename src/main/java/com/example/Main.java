package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() {
        // Hämtar databasinställningar från system properties/miljövariabler.
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        // Kontrollerar att alla inställningar finns, annars kasta ett tydligt fel.
        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables."
            );
        }
        // Skapar en anslutning till databasen.
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {

            // Skapar en Scanner för att läsa input från användaren.
            Scanner scanner = new Scanner(System.in);

            System.out.print("Username: "); // Ber användaren att skriva in användernamnet.
            String username = scanner.nextLine();

            System.out.print("Password: "); // Ber användaren att skriva in lösenordet.
            String password = scanner.nextLine();

            // En SQL-fråga för att kontrollera användarnamn och lösenord i databasen.
            String loginSql = "SELECT * FROM account WHERE name = ? AND password = ?";
            try (PreparedStatement stmt = connection.prepareStatement(loginSql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);

                try (ResultSet rs = stmt.executeQuery()) { // Om inget konto hittas, så misslyckas inlogget.
                    if (!rs.next()) {
                        System.out.println("Invalid login.");
                        return; // Avslutar run() om inloggningen misslyckas.
                    }
                }
            }

            System.out.println("Login successful!"); //Om inlogg lyckas.
            // TODO: Här kan du börja skriva meny-loop

        } catch (SQLException e) { //  Om något går fel med databasen, så kastas ett undantag.
            throw new RuntimeException(e);
        }
    }
    // Kollar om programmet körs i DevMode (ett utvecklingsläge).
    private static boolean isDevMode(String[] args) {
        // Kollar om VM option -DdevMode=true är tillsatt.
        if (Boolean.getBoolean("devMode")) return true;
        // Kollar om environment variabeln DEV_MODE=true är tillsatt.
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE"))) return true;
        return Arrays.asList(args).contains("--dev");
    }

    // Läser konfigurationen med system property först, sen environment variabeln.
    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey); // Kollar först system property.
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey); // Sedan miljövariabeln.
        }

        // Returnerar värdet eller null.
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}
