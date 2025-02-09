package com.example.poshaisan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OrderItemTest {

    private OrderItem item;

    @BeforeEach
    public void setUp() {
        item = new OrderItem("Item1", 100, 2, 1);
    }

    @Test
    public void testGetName() {
        assertThat(item.getName(), is("Item1"));
    }

    @Test
    public void testSetName() {
        item.setName("NewItem");
        assertThat(item.getName(), is("NewItem"));
    }

    @Test
    public void testGetPrice() {
        assertThat(item.getPrice(), is(100));
    }

    @Test
    public void testSetPrice() {
        item.setPrice(200);
        assertThat(item.getPrice(), is(200));
    }

    @Test
    public void testRecalculatePrice() {
        item.recalculatePrice(150);
        assertThat(item.getPrice(), is(300));
    }

    @Test
    public void testGetQuantity() {
        assertThat(item.getQuantity(), is(2));
    }

    @Test
    public void testSetQuantity() {
        item.setQuantity(5);
        assertThat(item.getQuantity(), is(5));
    }

    @Test
    public void testReduceQuantity() {
        item.reduceQuantity();
        assertThat(item.getQuantity(), is(1));
    }

    @Test
    public void testAddQuantity() {
        item.addQuantity(3);
        assertThat(item.getQuantity(), is(5));
    }

    @Test
    public void testGetId() {
        assertThat(item.getId(), is(1));
    }

    @Test
    public void testSetId() {
        item.setId(2);
        assertThat(item.getId(), is(2));
    }
}
