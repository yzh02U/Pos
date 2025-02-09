package com.example.poshaisan;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.WindowMatchers;
import org.testfx.matcher.control.TableViewMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testfx.api.FxAssert.verifyThat;

public class InventoryControllerTest extends ApplicationTest {

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    TableView<InventoryItem> inventoryTable;
    TextField filterInput;
    Button addProductBtn;
    private InventoryController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("inventory.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        this.inventoryTable = controller.inventoryTable;
        this.filterInput = controller.filterInput;
        this.addProductBtn = controller.addProductBtn;
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    public void setUp() {
        interact(() -> controller.loadInventoryData());
    }

    @Test
    public void testCheckAfterInitialize() {
        // Check that the view show the inventory stored in the DB
        assertThat(inventoryTable.getItems().size(), greaterThan(0));
    }

    @Test
    public void testChangeProductNameValid() {
        String TEST_PRODUCT_NAME = "PRODUCTO DE PRUEBA";
        for (Node node : lookup(".table-row-cell").queryAll()) {
            Node cell = node.lookup(".table-cell");


            if (cell instanceof TextFieldTableCell<?, ?> textFieldTableCell) {
                String cellText = textFieldTableCell.getText();
                if (cellText != null && cellText.equals("CARNE MONGOLIANA")) {
                    // Change product name
                    doubleClickOn(cell);
                    write(TEST_PRODUCT_NAME).push(KeyCode.ENTER)
                                            .push(KeyCode.ENTER);

                    // Verify that the test product does exist
                    verifyThat(inventoryTable, TableViewMatchers.containsRow(
                            TEST_PRODUCT_NAME
                            ,
                            8500,
                            "VACUNO Y CERDO"
                            , null));
                }
            }

        }

        // Revert the change
        for (Node node : lookup(".table-row-cell").queryAll()) {
            Node cell = node.lookup(".table-cell");

            if (cell instanceof TextFieldTableCell<?, ?> textFieldTableCell) {
                String cellText = textFieldTableCell.getText();
                if (cellText != null && cellText.equals(TEST_PRODUCT_NAME)) {
                    // Change product name
                    clickOn(cell).doubleClickOn(cell);
                    write("CARNE MONGOLIANA").push(KeyCode.ENTER)
                                             .push(KeyCode.ENTER);
                }
            }

        }
    }

    @Test
    public void testChangeProductNameInvalid() {
        for (Node node : lookup(".table-row-cell").queryAll()) {
            Node cell = node.lookup(".table-cell");


            if (cell instanceof TextFieldTableCell<?, ?> textFieldTableCell) {
                String cellText = textFieldTableCell.getText();
                if (cellText != null && cellText.equals("CARNE MONGOLIANA")) {
                    // Change product name for an invalid one, i.e, empty
                    doubleClickOn(cell);
                    write("").push(KeyCode.ENTER).push(KeyCode.ENTER);


                }
            }

            // Verify that the modified product kept his original name
            verifyThat(inventoryTable, TableViewMatchers.containsRow("CARNE " +
                                                                     "MONGOLIANA"
                    ,
                                                                     8500,
                                                                     "VACUNO " +
                                                                             "Y CERDO"
                    , null));

        }
    }

    @Test
    public void testChangeProductPriceValid() {
        for (Node node : lookup(".table-row-cell").queryAll()) {
            List<Node> cells = node.lookupAll(".table-cell").stream().toList();
            if (cells.size() > 1) {
                // Get the second column (price)
                Node firstColumnCell = cells.get(0);
                Node secondColumnCell = cells.get(1);
                if (firstColumnCell instanceof TextFieldTableCell<?, ?> firstColumnTextField &&
                    secondColumnCell instanceof IntegerEditingCell secondColumnTextField) {
                    String firstCellText = firstColumnTextField.getText();
                    String secondCellText = secondColumnTextField.getText();
                    if (firstCellText != null && firstCellText.equals("CARNE" +
                                                                      " MONGOLIANA")) {
                        // Change product name
                        clickOn(secondCellText).doubleClickOn(secondCellText);
                        write("1000").push(KeyCode.ENTER).push(KeyCode.ENTER);

                        // Verify that the test product does exist
                        verifyThat(inventoryTable,
                                   TableViewMatchers.containsRow("CARNE " +
                                                                 "MONGOLIANA"
                                           ,
                                                                 1000,
                                                                 "VACUNO Y " +
                                                                         "CERDO"
                                           , null));
                    }
                }
            }
        }

        // Revert the change
        for (Node node : lookup(".table-row-cell").queryAll()) {
            List<Node> cells = node.lookupAll(".table-cell").stream().toList();
            if (cells.size() > 1) {
                Node firstColumnCell = cells.get(0);
                Node secondColumnCell = cells.get(1);
                if (firstColumnCell instanceof TextFieldTableCell<?, ?> firstColumnTextField &&
                    secondColumnCell instanceof IntegerEditingCell secondColumnTextField) {
                    String firstCellText = firstColumnTextField.getText();
                    String secondCellText = secondColumnTextField.getText();
                    if (firstCellText != null && firstCellText.equals("CARNE" +
                                                                      " MONGOLIANA")) {
                        // Change product name
                        clickOn(secondCellText).doubleClickOn(secondCellText);
                        write("8500").push(KeyCode.ENTER).push(KeyCode.ENTER);

                    }
                }
            }
        }
    }

    @Test
    public void testChangeProductPriceInvalid() {
        for (Node node : lookup(".table-row-cell").queryAll()) {
            List<Node> cells = node.lookupAll(".table-cell").stream().toList();
            if (cells.size() > 1) {
                // Get the second column (price)
                Node firstColumnCell = cells.get(0);
                Node secondColumnCell = cells.get(1);
                if (firstColumnCell instanceof TextFieldTableCell<?, ?> firstColumnTextField &&
                    secondColumnCell instanceof IntegerEditingCell secondColumnTextField) {
                    String firstCellText = firstColumnTextField.getText();
                    String secondCellText = secondColumnTextField.getText();
                    if (firstCellText != null && firstCellText.equals("CARNE" +
                                                                      " MONGOLIANA")) {
                        // Change product name
                        clickOn(secondCellText).doubleClickOn(secondCellText);
                        write("").push(KeyCode.ENTER).push(KeyCode.ENTER);

                        // Verify that the price product price didn't change
                        verifyThat(inventoryTable,
                                   TableViewMatchers.containsRow("CARNE " +
                                                                 "MONGOLIANA"
                                           ,
                                                                 8500,
                                                                 "VACUNO Y " +
                                                                         "CERDO"
                                           , null));
                    }
                }
            }
        }
    }

    @Test
    public void testChangeProductType() {
        for (Node node : lookup(".table-row-cell").queryAll()) {
            List<Node> cells = node.lookupAll(".table-cell").stream().toList();
            if (cells.size() > 1) {
                // Get the third column (type)
                Node firstColumnCell = cells.get(0);
                Node thirdColumnCell = cells.get(2);

                if (firstColumnCell instanceof TextFieldTableCell<?, ?> firstColumnTextField &&
                    thirdColumnCell instanceof ComboBoxTableCell<?, ?>) {
                    String firstCellText = firstColumnTextField.getText();
                    if (firstCellText != null && firstCellText.equals("CARNE" +
                                                                      " MONGOLIANA")) {
                        // Change product name
                        clickOn(thirdColumnCell).doubleClickOn(thirdColumnCell);
                        clickOn("BEBIDAS").push(KeyCode.ENTER);

                        // Verify that the test product does exist
                        verifyThat(inventoryTable,
                                   TableViewMatchers.containsRow(
                                           "CARNE MONGOLIANA"
                                           ,
                                           8500,
                                           "BEBIDAS"
                                           , null));
                    }
                }
            }
        }

        // Revert the change
        for (Node node : lookup(".table-row-cell").queryAll()) {
            List<Node> cells = node.lookupAll(".table-cell").stream().toList();
            if (cells.size() > 1) {
                // Get the third column (type)
                Node firstColumnCell = cells.get(0);
                Node thirdColumnCell = cells.get(2);
                if (firstColumnCell instanceof TextFieldTableCell<?, ?> firstColumnTextField &&
                    thirdColumnCell instanceof ComboBoxTableCell<?, ?> ) {
                    String firstCellText = firstColumnTextField.getText();
                    if (firstCellText != null && firstCellText.equals("CARNE" +
                                                                      " MONGOLIANA")) {
                        // Simulate the use of scrollbar
                        doubleClickOn(thirdColumnCell);
                        for (int i = 0; i < 13; i++) {

                            type(KeyCode.DOWN);
                        }
                        push(KeyCode.ENTER);

                    }
                }
            }
        }
    }

    @Test
    public void testDeleteProduct() {
        // Add a temporal test item to the DB
        inventoryDAO.addInventoryToDatabase("TEST PRODUCT", 1000, "APERITIVOS");

        controller.loadInventoryData();
        inventoryTable.refresh();
        WaitForAsyncUtils.waitForFxEvents();
        // Verify that the item exists in the table
        verifyThat(inventoryTable, TableViewMatchers.containsRow(
                "TEST PRODUCT"
                ,
                1000,
                "APERITIVOS"
                , null));

        clickOn(controller.filterInput).write("TEST PRODUCT");

        // Delete the temporal item
        for (Node node : lookup(".table-row-cell").queryAll()) {
            List<Node> cells = node.lookupAll(".table-cell").stream().toList();
            if (cells.size() > 1) {

                // Get the fourth column (type)
                Node firstColumnCell = cells.get(0);
                Node fourthColumnCell = cells.get(3);
                if (firstColumnCell instanceof TextFieldTableCell<?, ?> firstColumnTextField) {
                    String firstCellText = firstColumnTextField.getText();
                    if (firstCellText != null && firstCellText.equals("TEST " +
                                                                      "PRODUCT")) {
                        // Delete the product
                        clickOn(fourthColumnCell);
                        push(KeyCode.ENTER);

                    }
                }
            }
        }

        // Check that the product got deleted
        verifyThat(inventoryTable, not(TableViewMatchers.containsRow(
                "TEST PRODUCT"
                ,
                1000,
                "APERITIVOS"
                , null)));

    }

    @Test
    public void testSearchProduct() {
        Integer originalQuantity = inventoryTable.getItems().size();
        clickOn(filterInput).write("CARNE");
        Integer filteredQuantity = inventoryTable.getItems().size();

        assertThat(filteredQuantity, lessThan(originalQuantity));

    }

    @Test
    public void testGoToAddProductView() {
        clickOn(addProductBtn);
        WaitForAsyncUtils.waitForFxEvents();
        // Check tha the new stage is showing
        verifyThat(window("Add Product"), WindowMatchers.isShowing());


    }

}
