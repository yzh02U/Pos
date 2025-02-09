package com.example.poshaisan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

/**
 * Represents an order associated with a table in a restaurant.
 */
public class TableOrder {

    // Fields --------------------------------------------------

    private final Integer id;
    private final ObservableList<OrderItem> items;
    private final LocalDateTime startDateTime;
    private String server;
    private String tableName;
    private Integer discount = 0;

    // Methods ------------------------------------------------

    /**
     * Constructs a TableOrder with the specified id, items, and start date.
     *
     * @param id            The unique identifier for the order.
     * @param items         The list of items in the order.
     * @param startDateTime The date and time when the order was started.
     */
    public TableOrder(Integer id, ObservableList<OrderItem> items,
                      LocalDateTime startDateTime) {
        this.id = id;
        this.items = items;
        this.startDateTime = startDateTime;
    }

    public TableOrder(Integer id, ObservableList<OrderItem> items,
                      LocalDateTime startDateTime, String server, String tableName) {
        this.id = id;
        this.items = items;
        this.startDateTime = startDateTime;
        this.server = server;
        this.tableName = tableName;
    }

    /**
     * Retrieves the server assigned to the table.
     *
     * @return The server assigned to the table.
     */
    public String getServer() {
        return (server != null) ? server : "-";
    }

    /**
     * Sets the server assigned to the table.
     *
     * @param server The server assigned to the table.
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * Retrieves the name of the table.
     *
     * @return The name of the table.
     */
    public String getTableName() {
        return (tableName != null) ? tableName : "-";
    }

    /**
     * Sets the name of the table.
     *
     * @param tableName The name of the table.
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Retrieves the unique identifier of the order.
     *
     * @return The unique identifier of the order.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Retrieves the list of items in the order.
     *
     * @return The list of items in the order.
     */
    public ObservableList<OrderItem> getItems() {
        return items;
    }

    /**
     * Retrieves the start date and time of the order.
     *
     * @return The start date and time of the order.
     */
    public LocalDateTime getStartDate() {
        return startDateTime;
    }

    /**
     * Adds an item to the order or updates its quantity and price if it
     * already exists.
     *
     * @param name     The name of the item to add or update.
     * @param quantity The quantity of the item.
     * @param price    The price per unit of the item.
     * @param id       The unique identifier of the item.
     * @return The updated quantity of the item.
     */
    public Integer addItem(String name, Integer quantity, Integer price,
                           Integer id, Integer initialQuantity) {
        OrderItem foundItem = null;

        for (OrderItem item : items) {
            if (item.getId().equals(id)) {
                foundItem = item;
                break;
            }
        }

        if (foundItem != null) {
            foundItem.addQuantity(quantity);
            foundItem.recalculatePrice(price / initialQuantity);
            return foundItem.getQuantity();
        } else {
            OrderItem newItem = new OrderItem(name, price * quantity, quantity,
                                              id);
            items.add(newItem);
            return quantity;
        }
    }

    /**
     * Reduces the quantity of an item in the order or removes it if quantity
     * becomes zero.
     *
     * @param id    The id of the item to reduce.
     * @param price The price per unit of the item.
     * @return The updated quantity of the item.
     */
    public Integer reduceItem(Integer id, Integer price,
                              Integer initialQuantity) {
        OrderItem foundItem = null;

        for (OrderItem item : items) {
            if (Objects.equals(id, item.getId())) {
                foundItem = item;
                break;
            }
        }

        if (foundItem != null) {
            foundItem.reduceQuantity();
            if (foundItem.getQuantity() == 0) {
                items.remove(foundItem);
                return 0;
            } else {
                foundItem.recalculatePrice(price / initialQuantity);
                return foundItem.getQuantity();
            }
        }
        return 0;
    }

    /**
     * Deletes an item from the order based on its id.
     *
     * @param id The id of the item to delete.
     */
    public void deleteItem(Integer id) {
        OrderItem itemToRemove = findItemById(id);

        if (itemToRemove != null) {
            items.remove(itemToRemove);
        }
    }

    /**
     * Sets the quantity and price of an existing item in the order, or adds
     * it if it doesn't exist.
     *
     * @param name     The name of the item to set or add.
     * @param quantity The quantity of the item.
     * @param price    The price per unit of the item.
     * @param id       The id of the item.
     */
    public void setItem(String name, Integer quantity, Integer price,
                        Integer id) {
        OrderItem foundItem = findItemById(id);

        if (foundItem != null) {
            foundItem.setQuantity(quantity);
            foundItem.setPrice(quantity * price);
        } else {
            OrderItem newItem = new OrderItem(name, price * quantity, quantity,
                                              id);
            items.add(newItem);
        }
    }

    /**
     * Calculates and retrieves the total sum of prices for all items in the
     * order.
     *
     * @return The total sum of prices for all items in the order.
     */
    public Integer getItemsSum() {
        return items.stream().mapToInt(OrderItem::getPrice).sum();
    }

    /**
     * Retrieves the discount applied to the order.
     *
     * @return The discount applied to the order.
     */
    public Integer getDiscount() {
        return discount;
    }

    /**
     * Sets the discount to be applied to the order.
     *
     * @param discount The discount to be applied to the order.
     */
    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    /**
     * Returns the maximum id inside current items.
     *
     * @return The maximum id inside current items.
     */
    public Integer getMaxId() {
        return items.stream().map(OrderItem::getId)
                    .max(Comparator.naturalOrder()).orElse(0);
    }

    /**
     * Sets the product name for an item based on its id.
     *
     * @param productName The new name of the product.
     * @param id          The id of the item to rename.
     */
    public void setProductName(String productName, Integer id) {
        OrderItem itemToRename = findItemById(id);

        if (itemToRename != null) {
            itemToRename.setName(productName);
        }
    }

    /**
     * Calculates and retrieves the suggested tip for the order (10% of the
     * total).
     *
     * @return The suggested tip for the order.
     */
    public Integer getTip() {
        Integer total = this.getItemsSum();
        return (int) (total * 0.1);
    }

    /**
     * Converts the list of items to a JSON string.
     *
     * @return JSON representation of the items list.
     * @throws JsonProcessingException If there is an error during the
     * conversion.
     */
    public String itemsToJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(items);
    }

    // Helper Methods --------------------------------------------------

    /**
     * Finds an OrderItem by its id in the list of items.
     *
     * @param id The id of the item to find.
     * @return The found OrderItem, or null if not found.
     */
    private OrderItem findItemById(Integer id) {
        return items.stream().filter(item -> Objects.equals(item.getId(), id))
                    .findFirst().orElse(null);
    }
}
