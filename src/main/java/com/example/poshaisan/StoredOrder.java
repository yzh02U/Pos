package com.example.poshaisan;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents an order stored in the system.
 */
public class StoredOrder {

    // Fields ---------------------------------------------------------

    private static final Utils utils = new Utils();
    private final Integer id;
    private final List<OrderItem> items;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final String server;
    private final boolean isTable;
    private final Integer total;
    private final String name;

    // Methods -----------------------------------------------------

    /**
     * Constructs a new StoredOrder with the specified parameters.
     *
     * @param id            The unique identifier for the order.
     * @param items         The items in the order as a JSON string.
     * @param isTable       Indicates whether the order is for a table or
     *                      takeout.
     * @param server        The server handling the order.
     * @param total         The total cost of the order.
     * @param startDateTime The start date and time of the order.
     * @param endDateTime   The end date and time of the order.
     * @throws IOException If an error occurs while parsing the items JSON
     * string.
     */
    public StoredOrder(Integer id, String items, boolean isTable, String server,
                       Integer total, LocalDateTime startDateTime,
                       LocalDateTime endDateTime, String name) throws IOException {
        this.id = id;
        this.server = server;
        this.total = total;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.items = utils.jsonToItems(items);
        this.isTable = isTable;
        this.name = name;
    }

    /**
     * Gets the server handling the order.
     *
     * @return The server handling the order.
     */
    public String getServer() {
        return server;
    }

    /**
     * Gets the unique identifier for the order.
     *
     * @return The unique identifier for the order.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Gets the list of items in the order.
     *
     * @return The list of items in the order.
     */
    public List<OrderItem> getItems() {
        return items;
    }

    /**
     * Gets the total cost of the order.
     *
     * @return The total cost of the order.
     */
    public Integer getTotal() {
        return total;
    }

    /**
     * Gets the start date and time of the order.
     *
     * @return The start date and time of the order.
     */
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * Gets the end date and time of the order.
     *
     * @return The end date and time of the order.
     */
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    /**
     * Indicates whether the order is for a table or takeout.
     *
     * @return True if the order is for a table, false if it is for takeout.
     */
    public boolean getIsTable() {
        return isTable;
    }

    public String getTableName(){
        return name;
    }
}

