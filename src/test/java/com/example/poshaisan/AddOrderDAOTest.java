package com.example.poshaisan;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class AddOrderDAOTest {

    private static final Logger logger = Logger.getLogger(
            AddOrderDAOTest.class.getName());
    private static AddOrderDAO addOrderDAO;
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
            connection = DriverManager.getConnection(
                    "jdbc:mariadb://localhost:3307/testdb", "root", "root");

            addOrderDAO = new AddOrderDAO(
                    "jdbc:mariadb://localhost:3307/testdb", "root", "root");

            try (var stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE orders ("
                             + "id INT PRIMARY KEY AUTO_INCREMENT, "
                             + "isTable BOOLEAN, "
                             + "products TEXT, "
                             + "server VARCHAR(255), "
                             + "startDateTime TIMESTAMP, "
                             + "endDateTime TIMESTAMP, "
                             + "total INT)");
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
                stmt.execute("DROP TABLE IF EXISTS orders");
            }
            connection.close();
        }
        if (db != null) {
            db.stop();
        }
    }

    @BeforeEach
    public void cleanDatabase() throws SQLException {
        try (var stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM orders");
        }
    }

    @Test
    public void testAddOrderToDatabase() {
        ObservableList<OrderItem> items = FXCollections.observableArrayList();
        items.add(new OrderItem("Test Product 1", 100, 2, 1));
        items.add(new OrderItem("Test Product 2", 200, 1, 2));

        TableOrder order = new TableOrder(1, items,
                                          LocalDateTime.now().minusHours(1));
        order.setServer("Test Server");
        order.setDiscount(50);

        LocalDateTime endDateTime = LocalDateTime.now();
        addOrderDAO.addOrderToDatabase(order, true, endDateTime);

        List<StoredOrder> orders = addOrderDAO.fetchOrdersFromDatabase();
        assertThat(orders, hasSize(1));
        StoredOrder storedOrder = orders.getFirst();
        assertThat(storedOrder.getTotal(), is(250));
        assertThat(storedOrder.getServer(), is("Test Server"));

    }

    @Test
    public void testFetchOrdersFromDatabase() {

        ObservableList<OrderItem> items = FXCollections.observableArrayList();
        items.add(new OrderItem("Another Product", 150, 1, 3));
        TableOrder order = new TableOrder(2, items,
                                          LocalDateTime.now().minusHours(2));
        addOrderDAO.addOrderToDatabase(order, true, LocalDateTime.now());

        List<StoredOrder> orders = addOrderDAO.fetchOrdersFromDatabase();

        assertThat(orders, hasSize(1));
    }

    @Test
    public void testResetOrders() {
        // Create one item that is greater than 1 week old
        ObservableList<OrderItem> oldItems =
                FXCollections.observableArrayList();
        oldItems.add(new OrderItem("Old Product", 100, 1, 4));
        TableOrder oldOrder = new TableOrder(3, oldItems,
                                             LocalDateTime.now().minusDays(10));
        addOrderDAO.addOrderToDatabase(oldOrder, true,
                                       LocalDateTime.now().minusDays(9));

        // Create one item that is less than 1 week old
        ObservableList<OrderItem> recentItems =
                FXCollections.observableArrayList();
        recentItems.add(new OrderItem("Recent Product", 200, 1, 5));
        TableOrder recentOrder = new TableOrder(4, recentItems,
                                                LocalDateTime.now()
                                                             .minusHours(1));
        addOrderDAO.addOrderToDatabase(recentOrder, true, LocalDateTime.now());

        // Reset order
        addOrderDAO.resetOrders();

        // Check that there's only one order left after reseting the orders.
        List<StoredOrder> orders = addOrderDAO.fetchOrdersFromDatabase();
        assertThat(orders, hasSize(1));
    }
}
