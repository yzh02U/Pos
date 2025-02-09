package com.example.poshaisan;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

public class InventoryItemTest {

    private InventoryItem inventoryItem;

    @BeforeEach
    public void setUp() {
        inventoryItem = new InventoryItem(1, "Test Item", 100, "Test Type");
    }

    @Test
    public void testGetItemName() {
        assertThat(inventoryItem.getItemName(), is("Test Item"));
    }

    @Test
    public void testSetItemName() {
        inventoryItem.setItemName("New Item Name");
        assertThat(inventoryItem.getItemName(), is("New Item Name"));
    }

    @Test
    public void testGetItemPrice() {
        assertThat(inventoryItem.getItemPrice(), is(100));
    }

    @Test
    public void testSetItemPrice() {
        inventoryItem.setItemPrice(200);
        assertThat(inventoryItem.getItemPrice(), is(200));
    }

    @Test
    public void testGetItemId() {
        assertThat(inventoryItem.getItemId(), is(1));
    }

    @Test
    public void testGetItemType() {
        assertThat(inventoryItem.getItemType(), is("Test Type"));
    }

    @Test
    public void testSetItemType() {
        inventoryItem.setItemType("New Type");
        assertThat(inventoryItem.getItemType(), is("New Type"));
    }

    @Test
    public void testItemNameProperty() {
        assertThat(inventoryItem.itemNameProperty(),
                   isA(SimpleStringProperty.class));
        assertThat(inventoryItem.itemNameProperty().get(), is("Test Item"));
    }

    @Test
    public void testItemPriceProperty() {
        assertThat(inventoryItem.itemPriceProperty(),
                   isA(SimpleIntegerProperty.class));
        assertThat(inventoryItem.itemPriceProperty().get(), is(100));
    }

    @Test
    public void testItemIdProperty() {
        assertThat(inventoryItem.itemIdProperty(),
                   isA(SimpleIntegerProperty.class));
        assertThat(inventoryItem.itemIdProperty().get(), is(1));
    }

    @Test
    public void testItemTypeProperty() {
        assertThat(inventoryItem.itemTypeProperty(),
                   isA(SimpleStringProperty.class));
        assertThat(inventoryItem.itemTypeProperty().get(), is("Test Type"));
    }
}
