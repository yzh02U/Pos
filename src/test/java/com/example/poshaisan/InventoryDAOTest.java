package com.example.poshaisan;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class InventoryDAOTest {

    private static final Logger logger =
            Logger.getLogger(InventoryDAOTest.class.getName());
    private static InventoryDAO inventoryDAO;
    private static DB db;
    private static Connection connection;

    @BeforeAll
    public static void setUp() throws SQLException {
        try {
            DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
            config.setPort(3307); // Puerto donde MariaDB4j ejecutará MySQL
            db = DB.newEmbeddedDB(config.build());
            db.start();
            db.createDB("testdb");
            connection = DriverManager.getConnection(
                    "jdbc:mariadb://localhost:3307/testdb", "root", "");

            inventoryDAO = new InventoryDAO(
                    "jdbc:mariadb://localhost:3307/testdb", "root", "");

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(
                        "CREATE TABLE inventory (id INT AUTO_INCREMENT " +
                        "PRIMARY KEY, name VARCHAR(255), price INT, type " +
                        "VARCHAR(255))");
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
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS inventory");
            }
            connection.close();
        }
        if (db != null) {
            db.stop();
        }
    }

    @AfterEach
    public void resetTable() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DELETE FROM inventory");
            }
        }
    }

    @Test
    public void testFetchInventoryFromDatabase() {
        inventoryDAO.addInventoryToDatabase("Test Item 1", 100, "Type 1");
        inventoryDAO.addInventoryToDatabase("Test Item 2", 200, "Type 2");
        List<InventoryItem> items = inventoryDAO.fetchInventoryFromDatabase();
        assertThat(items, hasSize(2));

        // Verify the first item
        InventoryItem firstItem = items.getFirst();
        assertThat(firstItem.getItemName(), is("Test Item 1"));
        assertThat(firstItem.getItemPrice(), is(100));
        assertThat(firstItem.getItemType(), is("Type 1"));

        // Verify the second item
        InventoryItem secondItem = items.getLast();
        assertThat(secondItem.getItemName(), is("Test Item 2"));
        assertThat(secondItem.getItemPrice(), is(200));
        assertThat(secondItem.getItemType(), is("Type 2"));
    }

    @Test
    public void testDeleteInventoryFromDatabase() {
        inventoryDAO.addInventoryToDatabase("Test Item 1", 100, "Type 1");
        List<InventoryItem> items = inventoryDAO.fetchInventoryFromDatabase();
        Integer firstItemId = items.getFirst().getItemId();
        boolean result = inventoryDAO.deleteInventoryFromDatabase(firstItemId);
        assertThat(result, is(true));

        // Check that there are no items left
        items = inventoryDAO.fetchInventoryFromDatabase();
        assertThat(items, hasSize(0));
    }

    @Test
    public void testAddInventoryToDatabase() {
        boolean result = inventoryDAO.addInventoryToDatabase("New Item", 300,
                                                             "New Type");
        assertThat(result, is(true));
        List<InventoryItem> items = inventoryDAO.fetchInventoryFromDatabase();

        assertThat(items, hasSize(1));

        InventoryItem firstItem = items.getFirst();
        assertThat(firstItem.getItemName(), is("New Item"));
        assertThat(firstItem.getItemPrice(), is(300));
        assertThat(firstItem.getItemType(), is("New Type"));
    }

    @Test
    public void testEditProductNameFromDatabase() {
        inventoryDAO.addInventoryToDatabase("Test Item 1", 100, "Type 1");
        List<InventoryItem> items = inventoryDAO.fetchInventoryFromDatabase();
        Integer firstItemId = items.getFirst().getItemId();
        boolean result = inventoryDAO.editProductNameFromDatabase(firstItemId,
                                                                  "Updated " +
                                                                  "Name");
        assertThat(result, is(true));
        assertThat(items, hasSize(1));

        items = inventoryDAO.fetchInventoryFromDatabase();
        String firstItemName = items.getFirst().getItemName();
        assertThat(firstItemName, is("Updated Name"));


    }

    @Test
    public void testEditProductPriceFromDatabase() {
        inventoryDAO.addInventoryToDatabase("Test Item 1", 100, "Type 1");
        List<InventoryItem> items = inventoryDAO.fetchInventoryFromDatabase();
        Integer firstItemId = items.getFirst().getItemId();
        boolean result = inventoryDAO.editProductPriceFromDatabase(firstItemId,
                                                                   500);
        assertThat(result, is(true));
        assertThat(items, hasSize(1));

        items = inventoryDAO.fetchInventoryFromDatabase();
        Integer firstItemPrice = items.getFirst().getItemPrice();
        assertThat(firstItemPrice, is(500));
    }

    @Test
    public void testEditProductTypeFromDatabase() {
        inventoryDAO.addInventoryToDatabase("Test Item 1", 100, "Type 1");
        List<InventoryItem> items = inventoryDAO.fetchInventoryFromDatabase();
        Integer firstItemId = items.getFirst().getItemId();
        boolean result = inventoryDAO.editProductTypeFromDatabase(firstItemId,
                                                                  "Updated " +
                                                                  "Type");
        assertThat(result, is(true));
        assertThat(items, hasSize(1));

        items = inventoryDAO.fetchInventoryFromDatabase();
        String firstItemType = items.getFirst().getItemType();
        assertThat(firstItemType, is("Updated Type"));
    }
}
