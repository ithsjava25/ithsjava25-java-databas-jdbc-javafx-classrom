package repositories;

public interface AccountInter {

    /**
     * Validates username and password.
     * @param password connected to username.
     * @param username of account that will be validated.
     * @return true if user exists in database, otherwise return false.
     */
    boolean validateLogIn(String username, String password);

    /**
     * Creates an account
     *
     * @param firstName users first name.
     * @param lastName  users last name.
     * @param password  user chooses a password.
     * @param ssn       users social security number.
     * Print confirmation if account was successfully created, otherwise print error-message
     */
    void createAccount(String firstName, String lastName, String password, String ssn);


    /**
     * Deletes an existing account.
     * @param userId primary key used to find account.
     * If account is not found print error message,
     * otherwise print confirmation
     */
    void deleteAccount(int userId);

    /**
     * Updates password of existing account
     * @param userID unique key used to find account
     * @param password new password
     * Prints confirmation when password successfully changed,
     * otherwise print error-message
     */
    void updatePassword(int userID, String password);

}
