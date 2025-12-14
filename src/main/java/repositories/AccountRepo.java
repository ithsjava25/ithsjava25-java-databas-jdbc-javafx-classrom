package repositories;

import com.example.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountRepo implements AccountInter{

    @Override
    public boolean validateLogIn(String username, String password) {
        String userQuery = "select * from account where name = ? and password = ?";

        try (Connection con = DataSource.getConnection();
             PreparedStatement pS = con.prepareStatement(userQuery)) {

            pS.setString(1, username);
            pS.setString(2, password);
            ResultSet result = pS.executeQuery();
            return result.next();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createAccount(String firstName, String lastName, String password, String ssn) {
        String addUser = "insert into account (name, password, first_name, last_name, ssn)" +
                "values (?, ?, ?, ?, ?)";

        try (Connection con = DataSource.getConnection();
             PreparedStatement pS = con.prepareStatement(addUser)) {

            String username = firstName.substring(0, 3) + lastName.substring(0, 3);
            pS.setString(1, username);
            pS.setString(2, password);
            pS.setString(3, firstName);
            pS.setString(4, lastName);
            pS.setString(5, ssn);
            int rowsAffected = pS.executeUpdate();

            if(rowsAffected == 0)
                System.out.println("Error. Something went wrong when creating the account.");
            else
                System.out.println("Account with username " + username + " successfully created.");


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAccount(int userId) {
        String deleteUser = "delete from account where user_id = ?";

        try (Connection con = DataSource.getConnection();
             PreparedStatement pS = con.prepareStatement(deleteUser)) {

            pS.setInt(1, userId);
            int rowsDeleted = pS.executeUpdate();

            if (rowsDeleted > 0)
                System.out.println("Account with id " + userId + " successfully deleted");
            else
                System.out.println("Error! No account found with id: " + userId);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updatePassword(int userID, String password) {
        String updatePassword = "update account set password = ? where user_id = ?";

        try (Connection con = DataSource.getConnection();
             PreparedStatement pS = con.prepareStatement(updatePassword)) {

            pS.setString(1, password);
            pS.setInt(2, userID);
            int rowsUpdated = pS.executeUpdate();

            if (rowsUpdated > 0)
                System.out.println("Password successfully updated.");
            else
                System.out.println("Error! Something went wrong when updating the password.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
