package com.example.poshaisan;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) class for handling user authentication.
 */
public class LoginDAO {

    private static final Logger logger = Logger.getLogger(
            LoginDAO.class.getName());
    private static final Utils utils = new Utils();
    private String URL = utils.DB_URL;
    private String USER = utils.DB_USER;
    private String PASSWORD = utils.DB_PASSWORD;

    /**
     * Default constructor that loads database credentials from environment
     * variables.
     */
    public LoginDAO() {
    }

    /**
     * Auxiliary constructor that uses the Testing Database credentials
     */
    public LoginDAO(String URL, String USER, String PASSWORD) {
        this.URL = URL;
        this.USER = USER;
        this.PASSWORD = PASSWORD;
    }

    /**
     * Checks if a user exists in the database with the given username and
     * password.
     *
     * @param username The username to check.
     * @param password The password to check.
     * @return true if the user exists in the database with the provided
     * credentials, otherwise false.
     */
    public boolean userExists(String username, String password) {
        String query = "SELECT * FROM users WHERE name = ? AND password = ?";

        try (Connection connection = Database.getConnection(URL, USER,
                                                            PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next(); // User exists with the given credentials
        } catch (Exception e) {
            logger.severe("An error occurred:");
            logger.severe(e.toString());
        }
        return false; // User does not exist or error occurred
    }
}
