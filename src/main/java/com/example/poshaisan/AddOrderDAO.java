package com.example.poshaisan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) class for adding orders to the database.
 */
public class AddOrderDAO {

    // Fields -------------------------------------------------

    private static final Logger logger = Logger.getLogger(
            AddOrderDAO.class.getName());
    private static final Utils utils = new Utils();
    private String URL = utils.DB_URL;
    private String USER = utils.DB_USER;
    private String PASSWORD = utils.DB_PASSWORD;

    // Constructors -------------------------------------------

    /**
     * Default constructor that loads database credentials from environment
     * variables.
     */
    public AddOrderDAO() {
    }

    /**
     * Auxiliary constructor that uses the Testing Database credentials
     */
    public AddOrderDAO(String URL, String USER, String PASSWORD) {
        this.URL = URL;
        this.USER = USER;
        this.PASSWORD = PASSWORD;
    }

    // Methods ------------------------------------------------

    /**
     * Adds a TableOrder to the database.
     *
     * @param order       The TableOrder to add.
     * @param isTable     A Boolean indicating if the order is for a table.
     * @param endDateTime The end date and time of the order.
     */
    public void addOrderToDatabase(TableOrder order, Boolean isTable, LocalDateTime endDateTime) {
        String query = "INSERT INTO orders (isTable, products, server, " +
                "startDateTime, endDateTime, total, name) VALUES (?, ?, ?, ?, ?, ?, ?)";

        if (order == null || order.getStartDate() == null || endDateTime == null ||
                order.getTableName() == null || order.getServer() == null) {
            logger.severe("Invalid order data: one or more required fields are null.");
            throw new IllegalArgumentException("Order data cannot be null");
        }

        Timestamp formattedStartDateTime = Timestamp.valueOf(order.getStartDate());
        Timestamp formattedEndDateTime = Timestamp.valueOf(endDateTime);
        int totalPrice = order.getItemsSum() - order.getDiscount();

        try (Connection connection = Database.getConnection(URL, USER, PASSWORD)) {
            if (connection == null || connection.isClosed()) {
                logger.severe("Database connection is null or closed");
                throw new SQLException("Failed to establish a database connection");
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setBoolean(1, isTable);
                preparedStatement.setString(2, order.itemsToJson());
                preparedStatement.setString(3, order.getServer());
                preparedStatement.setTimestamp(4, formattedStartDateTime);
                preparedStatement.setTimestamp(5, formattedEndDateTime);
                preparedStatement.setInt(6, totalPrice);
                preparedStatement.setString(7, order.getTableName());

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows == 0) {
                    logger.severe("Insert failed, no rows affected");
                    throw new SQLException("Creating order failed, no rows affected");
                }

                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            logger.severe("Error while adding order to database: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        } catch (JsonProcessingException e) {
            logger.severe("Error while processing order items to JSON: " + e.getMessage());
            throw new RuntimeException("JSON processing error: " + e.getMessage(), e);
        }
    }


    public void addOrderToDatabaseAndDelete(TableOrder order, Boolean isTable, LocalDateTime endDateTime) {
        String insertOrderQuery = "INSERT INTO orders (isTable, products, server, " +
                "startDateTime, endDateTime, total, name) VALUES (?, ?, ?, ?, ?, ?, ?)";

        String deleteCommandQuery = "DELETE FROM command WHERE name = ?";

        if (order == null || order.getStartDate() == null || endDateTime == null ||
                order.getTableName() == null || order.getServer() == null) {
            logger.severe("Invalid order data: one or more required fields are null.");
            throw new IllegalArgumentException("Order data cannot be null");
        }

        Timestamp formattedStartDateTime = Timestamp.valueOf(order.getStartDate());
        Timestamp formattedEndDateTime = Timestamp.valueOf(endDateTime);
        int totalPrice = order.getItemsSum() - order.getDiscount();

        try (Connection connection = Database.getConnection(URL, USER, PASSWORD)) {
            if (connection == null || connection.isClosed()) {
                logger.severe("Database connection is null or closed");
                throw new SQLException("Failed to establish a database connection");
            }

            // Desactivar auto-commit para controlar la transacción
            connection.setAutoCommit(false);

            try (
                    PreparedStatement insertStmt = connection.prepareStatement(insertOrderQuery);
                    PreparedStatement deleteStmt = connection.prepareStatement(deleteCommandQuery)
            ) {
                // --- INSERTAR EN ORDERS ---
                insertStmt.setBoolean(1, isTable);
                insertStmt.setString(2, order.itemsToJson());
                insertStmt.setString(3, order.getServer());
                insertStmt.setTimestamp(4, formattedStartDateTime);
                insertStmt.setTimestamp(5, formattedEndDateTime);
                insertStmt.setInt(6, totalPrice);
                insertStmt.setString(7, order.getTableName());

                int affectedRows = insertStmt.executeUpdate();
                if (affectedRows == 0) {
                    logger.severe("Insert failed, no rows affected");
                    throw new SQLException("Creating order failed, no rows affected");
                }

                // --- ELIMINAR DE COMMAND ---
                deleteStmt.setString(1, order.getTableName());
                int deletedRows = deleteStmt.executeUpdate();
                if (deletedRows > 0) {
                    logger.info("Order with name " + order.getTableName() + " deleted successfully from command.");
                } else {
                    logger.warning("No order found with name " + order.getTableName() + " in command.");
                }

                // --- COMMIT FINAL ---
                connection.commit();

            } catch (SQLException e) {
                connection.rollback(); // Si algo falla, deshacemos todo
                logger.severe("Transaction failed and rolled back: " + e.getMessage());
                throw e;
            }

        } catch (SQLException e) {
            logger.severe("Database error: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        } catch (JsonProcessingException e) {
            logger.severe("Error while processing order items to JSON: " + e.getMessage());
            throw new RuntimeException("JSON processing error: " + e.getMessage(), e);
        }

    }


    public void UpdateOrderInDatabase(TableOrder order, Boolean isTable,
                                      LocalDateTime endDateTime) {
        String query = "UPDATE command SET isTable = ?, products = ?, server = ?, " +
                "startDateTime = ?, endDateTime = ?, total = ?  WHERE name = ?";
        Timestamp formattedStartDateTime = Timestamp.valueOf(order.getStartDate());
        Timestamp formattedEndDateTime = Timestamp.valueOf(endDateTime);
        int totalPrice = order.getItemsSum() - order.getDiscount();

        try (Connection connection = Database.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameters for the prepared statement
            preparedStatement.setBoolean(1, isTable);
            preparedStatement.setString(2, order.itemsToJson());
            preparedStatement.setString(3, order.getServer());
            preparedStatement.setTimestamp(4, formattedStartDateTime);
            preparedStatement.setTimestamp(5, formattedEndDateTime);
            preparedStatement.setInt(6, totalPrice);
            preparedStatement.setString(7, order.getTableName());

            // Execute the update query
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                logger.info("Order updated successfully in the database.");
            } else {
                logger.warning("No order found with the specified name: " + order.getTableName());
            }

        } catch (SQLException e) {
            logger.severe("Error while updating order in database");
            logger.severe(e.toString());
        } catch (JsonProcessingException e) {
            logger.severe("Error while processing order items to JSON");
            logger.severe(e.toString());
        }
    }




    /**
     * Adds a TableOrder to the database.
     *
     * @param order       The TableOrder to add.
     * @param isTable     A Boolean indicating if the order is for a table.
     * @param endDateTime The end date and time of the order.
     */

    //Agrega la orden en command
    public void addOrderToDatabase_Command(TableOrder order, Boolean isTable,
                                   LocalDateTime endDateTime) {
        String query = "INSERT INTO command (isTable, products, server, " +
                "startDateTime, endDateTime, total, name) VALUES (?, " +
                "?, ?, " +
                "?, ?, ?, ?)";
        Timestamp formattedStartDateTime = Timestamp.valueOf(
                order.getStartDate());
        Timestamp formattedEndDateTime = Timestamp.valueOf(endDateTime);
        int totalPrice = order.getItemsSum() - order.getDiscount();

        try (Connection connection = Database.getConnection(URL, USER,
                PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     query)) {
            preparedStatement.setBoolean(1, isTable);
            preparedStatement.setString(2, order.itemsToJson());
            preparedStatement.setString(3, order.getServer());
            preparedStatement.setTimestamp(4, formattedStartDateTime);
            preparedStatement.setTimestamp(5, formattedEndDateTime);
            preparedStatement.setInt(6, totalPrice);
            preparedStatement.setString(7, order.getTableName());
            preparedStatement.executeUpdate();

            logger.severe("Orden Creada");

        } catch (SQLException e) {
            logger.severe("Error while adding order to database");
            logger.severe(e.toString());
        } catch (JsonProcessingException e) {
            logger.severe("Error while processing order items to JSON");
            logger.severe(e.toString());
        }
    }


    public void deleteOrderFromDatabase_Command(String name) {
        String query = "DELETE FROM command WHERE name = ?";

        try (Connection connection = Database.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the id parameter
            preparedStatement.setString(1, name);

            // Execute the delete operation
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Order with name " + name + " deleted successfully.");
            } else {
                logger.warning("No order found with name " + name + ".");
            }

        } catch (SQLException e) {
            logger.severe("Error while deleting order from database");
            logger.severe(e.toString());
        }
    }

    public void deleteAllOrdersFromDatabase() {
        String query = "TRUNCATE TABLE orders;";

        try (Connection connection = Database.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Ejecutar la operación de eliminación
            preparedStatement.executeUpdate();
            logger.info("SE HAN ELIMINADO TODAS LAS ORDENES DE ORDERS");

        } catch (SQLException e) {
            logger.severe("ERROR AL ELIMINAR DATOS DE LA TABLA ORDERS");
            logger.severe(e.toString());
        }
    }

    public void deleteAllOrdersFromDatabase_command() {
        String query = "TRUNCATE TABLE command;";

        try (Connection connection = Database.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Ejecutar la operación de eliminación
            preparedStatement.executeUpdate();
            logger.info("SE HAN ELIMINADO TODAS LAS ORDENES DE ORDERS");

        } catch (SQLException e) {
            logger.severe("ERROR AL ELIMINAR DATOS DE LA TABLA ORDERS");
            logger.severe(e.toString());
        }
    }
    /**
     * Fetches all orders from the database.
     *
     * @return List of StoredOrder objects containing order details.
     */
    public List<StoredOrder> fetchOrdersFromDatabase() {
        String query = "SELECT * FROM orders";
        List<StoredOrder> orders = new ArrayList<>();

        try (Connection connection = Database.getConnection(URL, USER,
                PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                LocalDateTime startDateTime = resultSet.getTimestamp(
                        "startDateTime").toLocalDateTime();
                LocalDateTime endDateTime = resultSet.getTimestamp(
                        "endDateTime").toLocalDateTime();

                orders.add(new StoredOrder(
                        resultSet.getInt("id"),
                        resultSet.getString("products"),
                        resultSet.getBoolean("isTable"),
                        resultSet.getString("server"),
                        resultSet.getInt("total"),
                        startDateTime, endDateTime,
                        resultSet.getString("name")));
            }
        } catch (SQLException e) {
            logger.severe("Problem while fetching orders from database");
            logger.severe(e.toString());
        } catch (IOException e) {
            logger.severe("Problem while converting order items from JSON");
            logger.severe(e.toString());
        }
        return orders;
    }


    public Integer getLastOrderIdFromDatabase_Command() {
        String query = "SELECT MAX(id) AS last_id FROM command";

        try (Connection connection = Database.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("last_id"); // Retorna el valor máximo del ID
            } else {
                logger.warning("No orders found in the database.");
                return 0;
            }

        } catch (SQLException e) {
            logger.severe("Error while retrieving last order ID from database");
            logger.severe(e.toString());
            return null;
        }
    }



    public List<Integer> getNonTableOrdersFromDatabase_Command() {
        String query = "SELECT id FROM command WHERE isTable = 0 ORDER BY id DESC";
        List<Integer> orderIds = new ArrayList<>();

        try (Connection connection = Database.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                orderIds.add(resultSet.getInt("id")); // Agrega las IDs de los pedidos
            }

            if (orderIds.isEmpty()) {
                logger.warning("No non-table orders found in the database.");
            }

        } catch (SQLException e) {
            logger.severe("Error while retrieving non-table orders from database");
            logger.severe(e.toString());
        }

        return orderIds; // Retorna la lista de IDs
    }


    public List<Integer> getNonTableOrdersFromDatabase() {
        String query = "SELECT id FROM orders WHERE isTable = 0 ORDER BY id DESC";
        List<Integer> orderIds = new ArrayList<>();

        try (Connection connection = Database.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                orderIds.add(resultSet.getInt("id")); // Agrega las IDs de los pedidos
            }

            if (orderIds.isEmpty()) {
                logger.warning("No non-table orders found in the database.");
            }

        } catch (SQLException e) {
            logger.severe("Error while retrieving non-table orders from database");
            logger.severe(e.toString());
        }

        return orderIds; // Retorna la lista de IDs
    }


    public List<StoredOrder> fetchOrdersFromDatabase_Command() {
        String query = "SELECT * FROM command";
        List<StoredOrder> orders = new ArrayList<>();

        try (Connection connection = Database.getConnection(URL, USER,
                PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                LocalDateTime startDateTime = resultSet.getTimestamp(
                        "startDateTime").toLocalDateTime();
                LocalDateTime endDateTime = resultSet.getTimestamp(
                        "endDateTime").toLocalDateTime();

                orders.add(new StoredOrder(
                        resultSet.getInt("id"),
                        resultSet.getString("products"),
                        resultSet.getBoolean("isTable"),
                        resultSet.getString("server"),
                        resultSet.getInt("total"),
                        startDateTime, endDateTime,
                        resultSet.getString("name")));

            }
        } catch (SQLException e) {
            logger.severe("Problem while fetching orders from database");
            logger.severe(e.toString());
        } catch (IOException e) {
            logger.severe("Problem while converting order items from JSON");
            logger.severe(e.toString());
        }
        return orders;
    }

    public List<StoredOrder> fetchDailyOrdersFromDatabase() {
        String query = "SELECT * FROM orders WHERE startDateTime >= ? AND endDateTime <= ?";
        LocalDate today = utils.getDateTime().toLocalDate();

        // Definir el rango de búsqueda
        LocalDateTime startOfDay = today.atTime(10, 0); // 10:00 hrs
        LocalDateTime endOfDay = today.atTime(23, 59); // 23:59 hrs

        List<StoredOrder> orders = new ArrayList<>();

        try (Connection connection = Database.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Establecer los valores de los parámetros de la consulta
            preparedStatement.setTimestamp(1, java.sql.Timestamp.valueOf(startOfDay));
            preparedStatement.setTimestamp(2, java.sql.Timestamp.valueOf(endOfDay));

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                LocalDateTime startDateTime = resultSet.getTimestamp("startDateTime").toLocalDateTime();
                LocalDateTime endDateTime = resultSet.getTimestamp("endDateTime").toLocalDateTime();

                orders.add(new StoredOrder(
                        resultSet.getInt("id"),
                        resultSet.getString("products"),
                        resultSet.getBoolean("isTable"),
                        resultSet.getString("server"),
                        resultSet.getInt("total"),
                        startDateTime, endDateTime,
                        resultSet.getString("name")));
            }
        } catch (SQLException e) {
            logger.severe("Problem while fetching orders from database");
            logger.severe(e.toString());
        } catch (IOException e) {
            logger.severe("Problem while converting order items from JSON");
            logger.severe(e.toString());
        }
        return orders;
    }


    /**
     * Resets (deletes) all orders before the current Monday
     *
     */
    public void resetOrders() {
        LocalDateTime today = utils.getDateTime();
        LocalDateTime thisWeekMonday =
                today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(8).withMinute(0);
        String query = "DELETE FROM orders WHERE endDateTime < ?";

        try (Connection connection = Database.getConnection(URL, USER,
                PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     query)) {
            preparedStatement.setTimestamp(1, Timestamp.valueOf(thisWeekMonday));
            int rowsAffected = preparedStatement.executeUpdate();
            logger.info("Deleted " + rowsAffected + " orders before " +
                    thisWeekMonday);

        } catch (SQLException e) {
            logger.severe("Problem while deleting orders from database");
            logger.severe(e.toString());
        }
    }


    public List<OrderItem> fetchOrderItems(int orderId) {
        List<OrderItem> orderItems = new ArrayList<>();
        String query = "SELECT products FROM orders WHERE id = ?"; // Cambio "items" por "products"

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, orderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String jsonProducts = resultSet.getString("products");
                if (jsonProducts != null && !jsonProducts.isEmpty()) {
                    try {
                        orderItems = utils.jsonToItems(jsonProducts); // Convertir JSON a lista de productos
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("Error al convertir JSON a lista de productos: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderItems;
    }



}