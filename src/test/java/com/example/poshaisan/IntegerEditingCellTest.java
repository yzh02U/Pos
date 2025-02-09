package com.example.poshaisan;

import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TableViewMatchers.hasTableCell;

public class IntegerEditingCellTest extends ApplicationTest {

    private TableView<InventoryItem> tableView;

    @Override
    public void start(Stage stage) {
        tableView = new TableView<>();
        tableView.setEditable(true);
        TableColumn<InventoryItem, Integer> priceColumn = new TableColumn<>(
                "Price");

        priceColumn.setCellValueFactory(
                cellData -> cellData.getValue().itemPriceProperty().asObject());
        priceColumn.setCellFactory(col -> new IntegerEditingCell());
        priceColumn.setEditable(true);

        tableView.getColumns().add(priceColumn);
        tableView.getItems().addAll(
                new InventoryItem(1, "Item 1", 100, "APERITIVOS"),
                new InventoryItem(2, "Item 2", 200, "APERITIVOS"),
                new InventoryItem(3, "Item 3", 300, "APERITIVOS")
        );

        stage.setScene(new Scene(tableView));
        stage.show();
    }

    @BeforeEach
    public void setUp() {
        interact(() -> {
        });
    }

    @Test
    public void testInitialCellValues() {
        verifyThat(tableView, hasTableCell("100"));
        verifyThat(tableView, hasTableCell("200"));
        verifyThat(tableView, hasTableCell("300"));
    }

    @Test
    public void testStartEdit() {
        clickOn(".table-cell").doubleClickOn(".table-cell").write("150")
                              .press(javafx.scene.input.KeyCode.ENTER);

        verifyThat(tableView, hasTableCell("150"));
        assertEquals(150, tableView.getItems().getFirst().getItemPrice());
    }

    @Test
    public void testCancelEdit() {
        clickOn(".table-cell").doubleClickOn(".table-cell").write("250")
                              .press(javafx.scene.input.KeyCode.ESCAPE);

        verifyThat(tableView, hasTableCell("100"));
        assertEquals(100, tableView.getItems().getFirst().getItemPrice());
    }

    @Test
    public void testInvalidInput() {
        clickOn(".table-cell").doubleClickOn(".table-cell").write("abc")
                              .press(javafx.scene.input.KeyCode.ENTER);

        verifyThat(tableView, hasTableCell("100"));
        assertEquals(100, tableView.getItems().getFirst().getItemPrice());
    }

    @Test
    public void testEmptyInput() {
        clickOn(".table-cell").doubleClickOn(".table-cell").eraseText(3)
                              .press(javafx.scene.input.KeyCode.ENTER);

        verifyThat(tableView, hasTableCell("100"));
        assertEquals(100, tableView.getItems().getFirst().getItemPrice());
    }


}
