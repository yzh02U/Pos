package com.example.poshaisan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections.
 */
public class Database {

    // Methods --------------------------------------------------

    /**
     * Retrieves a connection to the database using JDBC.
     *
     * @param URL      the URL of the database
     * @param USER     the database user
     * @param PASSWORD the password for the database user
     * @return a Connection object representing the database connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection(String URL, String USER,
                                           String PASSWORD) throws SQLException {
        if (URL == null || USER == null || PASSWORD == null) {
            throw new IllegalArgumentException(
                    "Database connection parameters cannot be null");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
