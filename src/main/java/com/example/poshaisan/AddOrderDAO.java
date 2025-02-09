package com.example.poshaisan;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    public void addOrderToDatabase(TableOrder order, Boolean isTable,
                                   LocalDateTime endDateTime) {
        String query = "INSERT INTO orders (isTable, products, server, " +
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

        } catch (SQLException e) {
            logger.severe("Error while adding order to database");
            logger.severe(e.toString());
        } catch (JsonProcessingException e) {
            logger.severe("Error while processing order items to JSON");
            logger.severe(e.toString());
        }
    }

    public void UpdateOrderInDatabase(TableOrder order, Boolean isTable,
                                      LocalDateTime endDateTime) {
        String query = "UPDATE command SET isTable = ?, products = ?, server = ?, " +
                "startDateTime = ?, endDateTime = ?, total = ?, name = ?  WHERE id = ?";
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
            preparedStatement.setString(8, order.getId().toString()); // Unique name for identification

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


    public void deleteOrderFromDatabase_Command(int id) {
        String query = "DELETE FROM command WHERE id = ?";

        try (Connection connection = Database.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the id parameter
            preparedStatement.setInt(1, id);

            // Execute the delete operation
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Order with ID " + id + " deleted successfully.");
            } else {
                logger.warning("No order found with ID " + id + ".");
            }

        } catch (SQLException e) {
            logger.severe("Error while deleting order from database");
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
        String query = "SELECT * FROM orders WHERE DATE(endDateTime) = ? ";
        LocalDate today = utils.getDateTime().toLocalDate();
        List<StoredOrder> orders = new ArrayList<>();

        try (Connection connection = Database.getConnection(URL, USER,
                PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     query)) {
            preparedStatement.setDate(1, java.sql.Date.valueOf(today));
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
}