package com.example.poshaisan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class that accesses the Database and processes any CRUD operations related to
 * the Inventory table. Interacts with InventoryController.
 */
public class InventoryDAO {

    // Fields --------------------------------------------------

    private static final Logger logger = Logger.getLogger(
            InventoryDAO.class.getName());
    private static final Utils utils = new Utils();
    private String URL = utils.DB_URL;
    private String USER = utils.DB_USER;
    private String PASSWORD = utils.DB_PASSWORD;

    // Constructors ---------------------------------------------

    /**
     * Default constructor that loads database credentials from environment
     * variables.
     */
    public InventoryDAO() {
    }

    /**
     * Auxiliary constructor that uses the Testing Database credentials
     */
    public InventoryDAO(String URL, String USER, String PASSWORD) {
        this.URL = URL;
        this.USER = USER;
        this.PASSWORD = PASSWORD;
    }

    // Methods -------------------------------------------------

    /**
     * Fetches all inventory items from the database.
     *
     * @return List of InventoryItem objects containing id, name, price, and
     * type.
     */
    public List<InventoryItem> fetchInventoryFromDatabase() {
        String query = "SELECT * FROM inventory";
        List<InventoryItem> array = new ArrayList<>();

        try (Connection connection = Database.getConnection(URL, USER,
                                                            PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     query)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                array.add(new InventoryItem(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("price"),
                        resultSet.getString("type")));
            }
        } catch (SQLException e) {
            logger.severe("Problem while fetching inventory from database");
            logger.severe(e.toString());
        }
        return array;
    }

    /**
     * Deletes an inventory item from the database based on the provided id.
     *
     * @param id The id of the inventory item to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public boolean deleteInventoryFromDatabase(Integer id) {
        String query = "DELETE FROM inventory WHERE id = ?";

        try (Connection connection = Database.getConnection(URL, USER,
                                                            PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     query)) {
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Problem while deleting inventory from database");
            logger.severe(e.toString());
            return false;
        }
    }

    /**
     * Adds a new inventory item to the database.
     *
     * @param name  The name of the new inventory item.
     * @param price The price of the new inventory item.
     * @param type  The type of the new inventory item.
     * @return true if addition was successful, false if the product already
     * exists.
     */
    public boolean addInventoryToDatabase(String name, Integer price,
                                          String type) {
        String query = "INSERT into inventory (name, price, type) VALUES (?, " +
                       "?, ?)";

        if (checkIfProductExists(name)) {
            return false;
        }

        try (Connection connection = Database.getConnection(URL, USER,
                                                            PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, price);
            preparedStatement.setString(3, type);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error while adding inventory to database");
            logger.severe(e.toString());
            return false;
        }
    }

    /**
     * Updates the name of an inventory item in the database.
     *
     * @param id   The id of the inventory item to update.
     * @param name The new name for the inventory item.
     * @return true if update was successful, false otherwise.
     */
    public boolean editProductNameFromDatabase(Integer id, String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        String query = "UPDATE inventory SET name = ? WHERE id = ?";

        try (Connection connection = Database.getConnection(URL, USER,
                                                            PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Problem while editing product name in database");
            logger.severe(e.toString());
            return false;
        }
    }

    /**
     * Updates the price of an inventory item in the database.
     *
     * @param id    The id of the inventory item to update.
     * @param price The new price for the inventory item.
     * @return true if update was successful, false otherwise.
     */
    public boolean editProductPriceFromDatabase(Integer id, Integer price) {
        if (price == null) {
            return false;
        }

        String query = "UPDATE inventory SET price = ? WHERE id = ?";

        try (Connection connection = Database.getConnection(URL, USER,
                                                            PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     query)) {
            preparedStatement.setInt(1, price);
            preparedStatement.setInt(2, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Problem while editing product price in database");
            logger.severe(e.toString());
            return false;
        }
    }

    /**
     * Updates the type of inventory item in the database.
     *
     * @param id   The id of the inventory item to update.
     * @param type The new type for the inventory item.
     * @return true if update was successful, false otherwise.
     */
    public boolean editProductTypeFromDatabase(Integer id, String type) {
        String query = "UPDATE inventory SET type = ? WHERE id = ?";

        try (Connection connection = Database.getConnection(URL, USER,
                                                            PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     query)) {
            preparedStatement.setString(1, type);
            preparedStatement.setInt(2, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Problem while editing product type in database");
            logger.severe(e.toString());
            return false;
        }
    }

    /**
     * Checks if a product with the given name already exists in the database.
     *
     * @param name The name of the product to check.
     * @return true if the product exists, false otherwise.
     */
    private boolean checkIfProductExists(String name) {
        String productName = name.toUpperCase();
        String query = "SELECT * FROM inventory WHERE UPPER(name) = ?";

        try (Connection connection = Database.getConnection(URL, USER,
                                                            PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     query)) {
            preparedStatement.setString(1, productName);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            logger.severe(
                    "Problem while checking if product exists in database");
            logger.severe(e.toString());
            return false;
        }
    }
}
