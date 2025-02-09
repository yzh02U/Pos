package com.example.poshaisan;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LoginDAOTest {

    private static final Logger logger = Logger.getLogger(
            LoginDAOTest.class.getName());
    private static LoginDAO loginDAO;
    private static DB db;
    private static Connection connection;

    @BeforeAll
    public static void setUp() throws SQLException {
        try {
            DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
            config.setPort(3307);
            db = DB.newEmbeddedDB(config.build());
            db.start();
            db.createDB("testdb");
            connection = DriverManager.getConnection("jdbc:mariadb" +
                                                     "://localhost:3307" +
                                                     "/testdb", "root", "root");

            loginDAO = new LoginDAO("jdbc:mariadb://localhost:3307/testdb",
                                    "root", "root");

            try (var stmt = connection.createStatement()) {
                stmt.execute(
                        "CREATE TABLE users (name VARCHAR(255) PRIMARY KEY, " +
                        "password VARCHAR(255))");
                stmt.execute(
                        "INSERT INTO users (name, password) VALUES " +
                        "('testUser', 'testPass')");
            }
        } catch (Exception e) {
            logger.severe("Failed to set up database: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    @AfterAll
    public static void closeConnection() throws SQLException,
            ManagedProcessException {
        if (connection != null && !connection.isClosed()) {
            try (var stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS users");
            }
            connection.close();
        }
        if (db != null) {
            db.stop();
        }
    }

    @Test
    public void testUserExistsUserFound() {
        boolean userExists = loginDAO.userExists("testUser", "testPass");
        assertThat(userExists, is(true));
    }

    @Test
    public void testUserExistsUserNotFound() {
        boolean userExists = loginDAO.userExists("testUser", "wrongPass");
        assertThat(userExists, is(false));
    }

    @Test
    public void testUserExistsUserNotExists() {
        boolean userExists = loginDAO.userExists("noSuchUser", "noPass");
        assertThat(userExists, is(false));
    }
}
