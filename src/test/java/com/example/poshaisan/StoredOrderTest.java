package com.example.poshaisan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StoredOrderTest {

    private StoredOrder order;
    private LocalDateTime now;

    @BeforeEach
    public void setUp() throws IOException {
        now = LocalDateTime.now();
        String itemsJson = "[{\"name\":\"item1\",\"quantity\":2}]]";
        order = new StoredOrder(1, itemsJson, false, "Server", 1, now, now,
                                "tableName");
    }

    @Test
    public void testGetId() {
        assertThat(order.getId(), is(1));
    }

    @Test
    public void testGetItems() throws IOException {
        List<OrderItem> expectedItems = new Utils().jsonToItems(
                "[{\"name\":\"item1\",\"quantity\":2}]");
        for (OrderItem item : order.getItems()) {
            for (OrderItem auxItem : expectedItems) {
                assertThat(item.getName(), is(auxItem.getName()));
            }
        }
    }

    @Test
    public void testGetIsTable() {
        assertThat(order.getIsTable(), is(false));
    }

    @Test
    public void testGetServer() {
        assertThat(order.getServer(), is("Server"));
    }

    @Test
    public void testGetTotal() {
        assertThat(order.getTotal(), is(1));
    }

    @Test
    public void testGetStartDateTime() {
        assertThat(order.getStartDateTime(), is(now));
    }

    @Test
    public void testGetEndDateTime() {
        assertThat(order.getEndDateTime(), is(now));
    }
}

