package com.example;

import java.sql.*;

public class JdbcAccountRepository implements AccountRepository{

    //Fetching Datasource object
    private final DataSource dataSource;


    //injection the datasource object into the constructor:
    public JdbcAccountRepository(DataSource dataSource){
        this.dataSource=dataSource;
    }


    @Override
    public boolean isValidLogin(String username, String password) {
        String sql = "SELECT COUNT(*) FROM account WHERE name = ? AND password = ?";

        try(
                Connection connection= dataSource.getConnection();
                PreparedStatement ps= connection.prepareStatement(sql)
                ) {
                ps.setString(1, username);
                ps.setString(2, password);

                try(ResultSet rs = ps.executeQuery()) {
                    if (rs.next()){
                        return rs.getInt(1)>0;
                    }
                }

        } catch (SQLException e){
            System.err.println("Database error during login validation: " + e.getMessage());
            return false;
        }

        return false;

    }

    @Override
    public boolean createAccount(String firstName, String lastName, String ssn, String password) {

        // concatenation of firstname and lastname to create username (name column in sql) for proper usage of the app.
        String username = firstName.substring(0, Math.min(firstName.length(), 3)) +
                lastName.substring(0, Math.min(lastName.length(), 3));

        String sql= "INSERT INTO account (first_name, last_name, ssn, password, name) VALUES (?, ?, ?, ?, ?)";

        try(Connection connection= dataSource.getConnection();
            PreparedStatement ps= connection.prepareStatement(sql)){

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, ssn);
            ps.setString(4, password);
            ps.setString(5, username);

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

        try(Connection connection= dataSource.getConnection();
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

        try(Connection connection= dataSource.getConnection();
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
