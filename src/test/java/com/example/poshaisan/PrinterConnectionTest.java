package com.example.poshaisan;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.awt.print.PrinterException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PrinterConnectionTest extends ApplicationTest {

    private static final Logger logger =
            Logger.getLogger(PrinterConnectionTest.class
                                     .getName());
    private PrinterConnection printerConnection;
    private TableOrder order;

    @BeforeEach
    public void setUp() {
        printerConnection = new PrinterConnection();

        ObservableList<OrderItem> items = FXCollections.observableArrayList(
                new OrderItem("Producto1", 1000, 2, 1),
                new OrderItem("Producto2", 2000, 1, 2)
        );

        LocalDateTime now = LocalDateTime.now();
        order = new TableOrder(1, items, now);
    }

    @Test
    public void testPrintOrder() {
        try {
            printerConnection.printOrder(order, true, false);

            // Verify if the .txt has been created
            String content = Files.readString(Paths.get("impresion.txt"));
            assertThat(content, is(not(emptyString())));

            // Verify its content
            assertThat(content, containsString("Producto1"));
            assertThat(content, containsString("Producto2"));
        } catch (PrinterException | IOException e) {
            logger.severe("Error while printing test");
        }
    }

    @Test
    public void testPrintOrderFileCreation() {
        try {
            printerConnection.printOrder(order, true, false);

            // Verify if the .txt has been created
            assertThat(Files.exists(Paths.get("impresion.txt")), is(true));
        } catch (PrinterException e) {
            logger.severe("Error while printing test");
        }
    }

    @Test
    public void testPrintOrderPrintingFailed() {
        try {
            printerConnection.printOrder(order, true,false);
        } catch (PrinterException e) {
            assertThat(e.getMessage(), is("Printing failed"));
        }
    }

    @Test
    public void testPrintOrderFileNotFound() {
        try {
            Files.deleteIfExists(Paths.get("impresion.txt"));
            printerConnection.printOrder(order, true, false);

        } catch (IOException e) {
            assertThat(e.getMessage(), is("File not found"));
        } catch (PrinterException e) {
            logger.severe("Error while printing test");
        }
    }
}