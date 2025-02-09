package com.example.poshaisan;

/**
 * Represents an item in an order.
 */
public class OrderItem {

    // Fields --------------------------------------------------

    private Integer id;
    private String name;      // Name of the item
    private Integer price;    // Total price of the item (price per unit *
    // quantity)
    private Integer quantity; // Quantity of the item

    // Methods ------------------------------------------------

    /**
     * Default constructor for OrderItem. Used for JSON deserialization.
     */
    public OrderItem() {
    }

    /**
     * Constructs an OrderItem with the specified name, price, quantity, and id.
     *
     * @param name     The name of the item.
     * @param price    The total price of the item.
     * @param quantity The quantity of the item.
     * @param id       The unique identifier of the item.
     */
    public OrderItem(String name, Integer price, Integer quantity, Integer id) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    /**
     * Retrieves the name of the item.
     *
     * @return The name of the item.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the item.
     *
     * @param name The name of the item to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the total price of the item.
     *
     * @return The total price of the item.
     */
    public Integer getPrice() {
        return price;
    }

    /**
     * Sets the total price of the item.
     *
     * @param price The total price of the item to set.
     */
    public void setPrice(Integer price) {
        this.price = price;
    }

    /**
     * Recalculates the total price of the item based on a new price per unit.
     *
     * @param unitPrice The new price per unit to recalculate the total price.
     */
    public void recalculatePrice(Integer unitPrice) {
        this.price = this.quantity * unitPrice;
    }

    /**
     * Retrieves the quantity of the item.
     *
     * @return The quantity of the item.
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the item.
     *
     * @param quantity The quantity of the item to set.
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        if (this.price != null) {
            this.recalculatePrice(this.price /
                                  this.quantity); // Adjust total price when
            // quantity changes
        }
    }

    /**
     * Reduces the quantity of the item by one.
     */
    public void reduceQuantity() {
        if (this.quantity > 0) {
            this.quantity -= 1;
        }
    }

    /**
     * Increases the quantity of the item by the specified amount.
     *
     * @param quantity The quantity to add to the item.
     */
    public void addQuantity(Integer quantity) {
        this.quantity += quantity;
    }

    /**
     * Retrieves the id of the item.
     *
     * @return The id of the item.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id of the item.
     *
     * @param id The id of the item to set.
     */
    public void setId(Integer id) {
        this.id = id;
    }
}
