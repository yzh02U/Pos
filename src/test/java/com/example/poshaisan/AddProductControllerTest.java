package com.example.poshaisan;

import ch.vorburger.exec.ManagedProcessException;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.TableViewMatchers;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testfx.api.FxAssert.verifyThat;

class AddProductControllerTest extends ApplicationTest {

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private InventoryController inventoryController;
    private Button cancelBtn;
    private Button confirmBtn;
    private TextField nameInput;
    private Label nameLabelWarning;
    private TextField priceInput;
    private Label priceLabelWarning;
    private ComboBox<String> typeInput;
    private Label typeLabelWarning;
    private Stage addProductStage;

    @Override
    public void start(
            Stage stage) throws ManagedProcessException, SQLException,
            IOException {
        this.addProductStage = stage;

        // Set up the controller and its dependencies
        FXMLLoader inventoryLoader = new FXMLLoader(
                getClass().getResource("inventory.fxml"));
        inventoryLoader.load();
        this.inventoryController = inventoryLoader.getController();


        FXMLLoader loader = new FXMLLoader(getClass().getResource("add" +
                                                                  "-product" +
                                                                  ".fxml"));
        Parent root = loader.load();
        AddProductController controller = loader.getController();

        controller.setInventoryController(inventoryController);
        this.cancelBtn = controller.cancelBtn;
        this.confirmBtn = controller.confirmBtn;
        this.nameInput = controller.nameInput;
        this.nameLabelWarning = controller.nameLabelWarning;
        this.priceInput = controller.priceInput;
        this.priceLabelWarning = controller.priceLabelWarning;
        this.typeInput = controller.typeInput;
        this.typeLabelWarning = controller.typeLabelWarning;

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();


    }


    @Test
    public void testValidProductName() {
        // Check that there's a warning before filling the input
        assertThat(nameLabelWarning.isVisible(), is(true));
        clickOn(nameInput).write("Product Name");

        // Check that there's no warning after filling the input
        assertThat(nameLabelWarning.isVisible(), is(false));

    }

    @Test
    public void testInvalidProductName() {
        // Check that there's a warning before filling the input
        assertThat(nameLabelWarning.isVisible(), is(true));

        // Empty value should not be allowed at product name
        clickOn(nameInput).write("");

        // The warning should still be visible
        assertThat(nameLabelWarning.isVisible(), is(true));

    }


    @Test
    public void testValidProductPrice() {
        assertThat(priceLabelWarning.isVisible(), is(true));

        clickOn(priceInput).write("1000");

        assertThat(priceLabelWarning.isVisible(), is(false));
    }

    @Test
    public void testInvalidProductPrice() {
        assertThat(priceLabelWarning.isVisible(), is(true));

        clickOn(priceInput).write("This text should not be allowed");

        // The warning should still be visible
        assertThat(priceLabelWarning.isVisible(), is(true));
    }

    @Test
    public void testProductType() {
        assertThat(typeLabelWarning.isVisible(), is(true));

        clickOn(typeInput).clickOn("APERITIVOS");

        assertThat(typeLabelWarning.isVisible(), is(false));
    }

    @Test
    public void testAddProduct() {
        Integer originalAmount =
                inventoryController.inventoryTable.getItems().size();

        clickOn(nameInput).write("Product Name");
        clickOn(priceInput).write("1000");
        clickOn(typeInput).clickOn("APERITIVOS");
        clickOn(confirmBtn);

        inventoryController.loadInventoryData();
        Integer updatedAmount =
                inventoryController.inventoryTable.getItems().size();

        assertThat(updatedAmount, greaterThan(originalAmount));

        // Verify that the product name it's capitalized
        verifyThat(inventoryController.inventoryTable,
                   TableViewMatchers.containsRow(
                           "PRODUCT NAME"
                           ,
                           1000,
                           "APERITIVOS"
                           , null));

        // Delete the test item
        for (InventoryItem item :
                inventoryController.inventoryTable.getItems()) {
            if (item.getItemName().equals("PRODUCT NAME")) {
                inventoryDAO.deleteInventoryFromDatabase(item.getItemId());
            }
        }

    }

    @Test
    public void testAddProductInvalidName() {
        Integer originalAmount =
                inventoryController.inventoryTable.getItems().size();
        clickOn(priceInput).write("1000");
        clickOn(confirmBtn);
        WaitForAsyncUtils.waitForFxEvents();
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(
                DialogPane.class);
        assertThat(dialogPane, is(notNullValue()));
        assertThat(dialogPane.getContentText(),
                   is("El nombre del producto está vacio"));

        inventoryController.loadInventoryData();
        Integer updatedAmount =
                inventoryController.inventoryTable.getItems().size();

        assertThat(originalAmount, is(updatedAmount));


    }

    @Test
    public void testAddProductInvalidPrice() {
        Integer originalAmount =
                inventoryController.inventoryTable.getItems().size();
        clickOn(nameInput).write("Invalid Product");
        clickOn(confirmBtn);
        WaitForAsyncUtils.waitForFxEvents();
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(
                DialogPane.class);
        assertThat(dialogPane, is(notNullValue()));
        assertThat(dialogPane.getContentText(),
                   is("El precio del producto está vacio"));

        inventoryController.loadInventoryData();
        Integer updatedAmount =
                inventoryController.inventoryTable.getItems().size();

        assertThat(originalAmount, is(updatedAmount));
    }

    @Test
    public void testAddProductInvalidType() {
        Integer originalAmount =
                inventoryController.inventoryTable.getItems().size();
        clickOn(nameInput).write("Invalid Product");
        clickOn(priceInput).write("1000");
        clickOn(confirmBtn);
        WaitForAsyncUtils.waitForFxEvents();
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(
                DialogPane.class);
        assertThat(dialogPane, is(notNullValue()));
        assertThat(dialogPane.getContentText(),
                   is("El tipo del producto está vacio"));

        inventoryController.loadInventoryData();
        Integer updatedAmount =
                inventoryController.inventoryTable.getItems().size();

        assertThat(originalAmount, is(updatedAmount));


    }

    @Test
    public void testCloseStage() {
        Platform.runLater(() -> clickOn(cancelBtn));

        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> assertThat(addProductStage.isShowing(), is(false)));
    }

}
