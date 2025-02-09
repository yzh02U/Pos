package com.example.poshaisan;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

public class AddOrderControllerTest extends ApplicationTest {

    private static final Utils utils = new Utils();
    private static final InventoryDAO inventoryDAO = new InventoryDAO();
    private final Random random = new Random();
    List<InventoryItem> items = inventoryDAO.fetchInventoryFromDatabase();
    private AddOrderController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("add" +
                                                                  "-order" +
                                                                  ".fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        ObservableList<OrderItem> items = FXCollections.observableArrayList();
        LocalDateTime now = LocalDateTime.now();
        ObservableList<TableOrder> tables =
                TablesList.getInstance().getTablesList();
        TableOrder auxiliaryTable = new TableOrder(1, items, now);
        auxiliaryTable.setTableName("Test");
        tables.add(auxiliaryTable);
        TableOrder order = new TableOrder(2, items, now);
        controller.initTableOrder(order, true);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testServerInput() {
        List<String> servers = utils.getSERVERS();
        for (String server : servers) {

            // Reset the input text
            controller.serverInput.setText("");
            clickOn(controller.serverInput).write(server.substring(0, 2));
            type(KeyCode.DOWN);
            type(KeyCode.ENTER);

            // Verify that the input has the full server name
            verifyThat(controller.serverInput, hasText(server));
        }
    }

    @Test
    public void testInvalidTableInput() {
        controller.tableInput.setText("");

        // Try to set an invalid value (duplicate table)
        clickOn(controller.tableInput).write("Test");
        type(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();

        // Dismiss the dialog
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(
                DialogPane.class);
        assertThat(dialogPane, is(notNullValue()));
        assertThat(dialogPane.getContentText(), is("La mesa ya existe"));
        Button confirmBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
        clickOn(confirmBtn);

        // Check that the table input is empty because its value was invalid
        assertThat(controller.tableInput, hasText(""));
    }

    @Test
    public void testValidTableInput() {
        controller.tableInput.setText("");

        // Try to set a valid value
        clickOn(controller.tableInput).write("Test2");
        type(KeyCode.ENTER);

        // Check that the table input has the same value
        assertThat(controller.tableInput, hasText("Test2"));
    }

    @Test
    public void testProductInput() {
        int index = random.nextInt(items.size());

        // Get a random product
        InventoryItem randomItem = items.get(index);
        String randomItemSubstring = randomItem.getItemName();

        // Check if the item exists in the AutoCompletion TextField
        clickOn(controller.productInput).write(randomItemSubstring);
        type(KeyCode.DOWN);
        type(KeyCode.ENTER);
        // Verify that the input has the full product name
        verifyThat(controller.productInput, hasText(randomItem.getItemName()));
    }

    @Test
    public void testProductInvalidProductQuantity() {
        // Try writing an invalid input, i.e, strings
        clickOn(controller.quantityInput).write("Invalid Input");

        // Check that the input has no text.
        assertThat(controller.quantityInput, hasText(""));
    }

    @Test
    public void testProductValidProductQuantity() {
        // Try writing an invalid input, i.e, strings
        clickOn(controller.quantityInput).write("12");

        // Check that the input has no text.
        assertThat(controller.quantityInput, hasText("12"));
    }

    @Test
    void addProductViaInput() {
        InventoryItem randomItem = getRandomItem();

        // Add the quantity

        write("2");
        type(KeyCode.ENTER);

        assertThat(controller.productTable.getItems(), hasSize(1));
        assertThat(controller.productTable.getItems().getFirst().getName(),
                   is(randomItem.getItemName()));

        assertThat(controller.productTable.getItems().getFirst().getQuantity(),
                   is(2));
    }

    @Test
    public void addProductViaLayout() {
        ObservableList<Node> flowpaneChildren =
                controller.flowpaneContainer.getChildren();

        // Click the first instance of a dish
        clickFirstDish:
        for (Node typesChild : flowpaneChildren) {
            if (typesChild instanceof VBox) {
                for (Node dish : ((VBox) typesChild).getChildren()) {
                    doubleClickOn(dish);
                    break clickFirstDish;
                }
            }
        }

        assertThat(controller.productTable.getItems(), hasSize(1));
        assertThat(controller.productTable.getItems().getFirst().getQuantity(),
                   is(1));
    }

    private InventoryItem getRandomItem() {
        // Check that the table has no items at the beginning
        assertThat(controller.productTable.getItems(), hasSize(0));
        int index = random.nextInt(items.size());

        // Get a random product
        InventoryItem randomItem = items.get(index);
        String randomItemSubstring = randomItem.getItemName();

        clickOn(controller.productInput).write(randomItemSubstring);
        type(KeyCode.DOWN);
        type(KeyCode.ENTER);

        return randomItem;
    }


    @Test
    public void testModifyValidProductQuantity() {

        getRandomItem();

        write("1");
        type(KeyCode.ENTER);

        // Modify the product quantity via TextField
        clickOn(controller.productTable.lookup(".table-row-cell"));
        TextField productQuantityInput = lookup(
                "#product-quantity-input").queryAs(TextField.class);
        productQuantityInput.setText("");
        clickOn(productQuantityInput).write("2");
        type(KeyCode.ENTER);
        assertThat(controller.productTable.getItems().getFirst().getQuantity(),
                   is(2));
    }

    @Test
    public void testModifyInvalidProductQuantity() {

        getRandomItem();
        write("1");
        type(KeyCode.ENTER);

        // Insert an invalid quantity (empty)
        clickOn(controller.productTable.lookup(".table-row-cell"));
        TextField productQuantityInput = lookup(
                "#product-quantity-input").queryAs(TextField.class);
        productQuantityInput.setText("");
        clickOn(productQuantityInput).write("");
        type(KeyCode.ENTER);

        WaitForAsyncUtils.waitForFxEvents();

        // Check if alert is shown
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(
                DialogPane.class);
        assertThat(dialogPane, is(notNullValue()));
        assertThat(dialogPane.getContentText(), is("El campo no puede estar " +
                                                   "vacio"));

        Button confirmBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
        clickOn(confirmBtn);

        // Check that the quantity is the same as the original value
        assertThat(productQuantityInput.getText(), is("1"));
    }

    @Test
    public void testModifyValidProductPrice() {

        getRandomItem();

        write("1");
        type(KeyCode.ENTER);

        // Modify the product quantity via TextField
        clickOn(controller.productTable.lookup(".table-row-cell"));
        TextField productPriceInput =
                lookup("#product-price-input").queryAs(TextField.class);
        productPriceInput.setText("");
        clickOn(productPriceInput).write("2000");
        type(KeyCode.ENTER);
        assertThat(controller.productTable.getItems().getFirst().getPrice(),
                   is(2000));
    }

    @Test
    public void testModifyInvalidProductPrice() {

        getRandomItem();
        write("1");
        type(KeyCode.ENTER);

        // Insert an invalid quantity (empty)
        WaitForAsyncUtils.waitForFxEvents();
        Integer originalPrice =
                controller.productTable.getItems().getFirst().getPrice();
        clickOn(controller.productTable.lookup(".table-row-cell"));
        TextField productPriceInput =
                lookup("#product-price-input").queryAs(TextField.class);
        productPriceInput.setText("");
        clickOn(productPriceInput).write("");
        type(KeyCode.ENTER);

        WaitForAsyncUtils.waitForFxEvents();

        // Check if alert is shown
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(
                DialogPane.class);
        assertThat(dialogPane, is(notNullValue()));
        assertThat(dialogPane.getContentText(), is("El campo no puede estar " +
                                                   "vacio"));

        Button confirmBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
        clickOn(confirmBtn);

        // Check that the quantity is the same as the original value
        assertThat(productPriceInput.getText(),
                   is(Integer.toString(originalPrice)));
    }

    @Test
    public void testModifyProductViaButtons() {
        getRandomItem();
        write("1");
        type(KeyCode.ENTER);

        WaitForAsyncUtils.waitForFxEvents();
        assertThat(controller.productTable.getItems().getFirst().getQuantity(),
                   is(1));
        clickOn(controller.productTable.lookup(".table-row-cell"));
        Button addQuantityBtn =
                lookup("#product-add-btn").queryAs(Button.class);
        Button reduceQuantityBtn =
                lookup("#product-reduce-btn").queryAs(Button.class);
        Button deleteQuantityBtn =
                lookup("#product-delete-btn").queryAs(Button.class);

        clickOn(addQuantityBtn);

        // Check that the quantity has increased by 1.
        assertThat(controller.productTable.getItems().getFirst().getQuantity(),
                   is(2));

        clickOn(reduceQuantityBtn);

        // Check that the quantity has increased by 1.
        assertThat(controller.productTable.getItems().getFirst().getQuantity(),
                   is(1));

        clickOn(deleteQuantityBtn);

        assertThat(controller.productTable.getItems(),
                   hasSize(0));
    }
}
