package com.example.poshaisan;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


/**
 * Class that provides a structure of an Inventory Item. It contains multiple
 * setters and getters related to the properties it has.
 */
public class InventoryItem {

    // Fields --------------------------------------------------

    private final SimpleStringProperty itemName;
    private final SimpleIntegerProperty itemPrice;
    private final SimpleIntegerProperty itemId;
    private final SimpleStringProperty itemType;

    // Constructors --------------------------------------------

    /**
     * Constructor to initialize an InventoryItem object with specified
     * attributes.
     *
     * @param itemId    The ID of the inventory item.
     * @param itemName  The name of the inventory item.
     * @param itemPrice The price of the inventory item.
     * @param type      The type of the inventory item.
     */
    public InventoryItem(int itemId, String itemName, int itemPrice,
                         String type) {
        this.itemName = new SimpleStringProperty(itemName);
        this.itemPrice = new SimpleIntegerProperty(itemPrice);
        this.itemId = new SimpleIntegerProperty(itemId);
        this.itemType = new SimpleStringProperty(type);
    }

    // Methods -------------------------------------------------

    /**
     * Getter for itemName property.
     *
     * @return The name of the inventory item.
     */
    public String getItemName() {
        return itemName.get();
    }

    /**
     * Setter for itemName property.
     *
     * @param itemName The name of the inventory item to set.
     */
    public void setItemName(String itemName) {
        this.itemName.set(itemName);
    }

    /**
     * Property getter for itemName.
     *
     * @return SimpleStringProperty object representing itemName.
     */
    public SimpleStringProperty itemNameProperty() {
        return itemName;
    }

    /**
     * Getter for itemPrice property.
     *
     * @return The price of the inventory item.
     */
    public int getItemPrice() {
        return itemPrice.get();
    }


    /**
     * Setter for itemPrice property.
     *
     * @param itemPrice The price of the inventory item to set.
     */
    public void setItemPrice(int itemPrice) {
        this.itemPrice.set(itemPrice);
    }

    /**
     * Property getter for itemPrice.
     *
     * @return SimpleIntegerProperty object representing itemPrice.
     */
    public SimpleIntegerProperty itemPriceProperty() {
        return itemPrice;
    }

    /**
     * Getter for itemId property.
     *
     * @return The ID of the inventory item.
     */
    public Integer getItemId() {
        return itemId.get();
    }

    /**
     * Getter for itemId property.
     *
     * @return The SimpleIntegerProperty ID of the inventory item.
     */
    public SimpleIntegerProperty itemIdProperty() {
        return itemId;
    }

    /**
     * Getter for itemType property.
     *
     * @return The type of the inventory item.
     */
    public String getItemType() {
        return itemType.get();
    }

    /**
     * Setter for itemType property.
     *
     * @param itemType The type of the inventory item to set.
     */
    public void setItemType(String itemType) {
        this.itemType.set(itemType);
    }

    /**
     * Property getter for itemType.
     *
     * @return StringProperty object representing itemType.
     */
    public StringProperty itemTypeProperty() {
        return itemType;
    }
}
