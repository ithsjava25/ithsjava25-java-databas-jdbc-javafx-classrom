package com.example;

import java.sql.*;

public class JdbcAccountRepository implements AccountRepository{

    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPass;

    public JdbcAccountRepository(String jdbcUrl, String dbUser, String dbPass){
        this.jdbcUrl=jdbcUrl;
        this.dbUser=dbUser;
        this.dbPass=dbPass;
    }


    @Override
    public boolean isValidLogin(String username, String password) {
        String sql = "SELECT COUNT(*) FROM account WHERE name = ? AND password = ?";

        try(
                Connection connection= DriverManager.getConnection(jdbcUrl,dbUser,dbPass);
                PreparedStatement ps= connection.prepareStatement(sql);
                ) {
                ps.setString(1, username);
                ps.setString(2, password);

                try(ResultSet rs = ps.executeQuery()) {
                    if (rs.next()){
                        return rs.getInt(1)>0;
                    }
                }

        } catch (SQLException e){
            return false;
        }

        return false;

    }

    @Override
    public boolean createAccount(String firstName, String lastName, String ssn, String password) {
        String sql= "INSERT INTO account (first_name, last_name, ssn, password) VALUES (?, ?, ?, ?)";

        try(Connection connection= DriverManager.getConnection(jdbcUrl,dbUser,dbPass);
            PreparedStatement ps= connection.prepareStatement(sql)){

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, ssn);
            ps.setString(4, password);

            int rowsAffected= ps.executeUpdate();
            return rowsAffected>0;
        } catch (SQLException e){
            System.err.println("Could not create account: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updatePassword(long userId, String newPassword) {
        String sql= "UPDATE account SET password = ? WHERE user_id = ?";

        try(Connection connection= DriverManager.getConnection(jdbcUrl,dbUser,dbPass);
            PreparedStatement ps=connection.prepareStatement(sql)){

            ps.setString(1, newPassword);
            ps.setLong(2, userId);

            int rowsAffected=ps.executeUpdate();
            return rowsAffected>0;

        }catch (SQLException e){
            System.err.println(" Could not update password: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteAccount(long userId) {
        String sql= " DELETE FROM account WHERE user_id = ?";

        try(Connection connection= DriverManager.getConnection(jdbcUrl,dbUser,dbPass);
            PreparedStatement ps=connection.prepareStatement(sql)){

            ps.setLong(1, userId);

            int rowsAffected=ps.executeUpdate();
            return rowsAffected>0;

        }catch (SQLException e){
            System.err.println(" Could not delete account: " + e.getMessage());
            return false;
        }


    }
}
