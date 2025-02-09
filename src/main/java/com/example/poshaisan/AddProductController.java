package com.example.poshaisan;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;


/**
 * Controller class for adding a product.
 * Interacts with InventoryDAO and InventoryController.
 */
public class AddProductController {

    // Fields --------------------------------------------------------

    public VBox addProductSection;
    public final Utils utils = new Utils();
    public final List<String> DISH_TYPES = utils.getDISH_TYPES();
    private static final InventoryDAO inventoryDAO = new InventoryDAO();
    private static final CustomAlert customAlert = new CustomAlert();
    private InventoryController inventoryController;

    @FXML
    Button cancelBtn;
    @FXML
    Button confirmBtn;
    @FXML
    TextField nameInput;
    @FXML
    Label nameLabelWarning;
    @FXML
    TextField priceInput;
    @FXML
    Label priceLabelWarning;
    @FXML
    ComboBox<String> typeInput;
    @FXML
    Label typeLabelWarning;

    // Methods --------------------------------------------------------

    /**
     * Initializes the controller.
     * Sets up a Listener for the Price Input to allow only numeric values
     * and sets up the options for the Type Input
     */
    public void initialize() {
        changePriceInputType();
        ObservableList<String> dishTypes = FXCollections.observableArrayList(
                DISH_TYPES);

        typeInput.setItems(dishTypes);
    }

    /**
     * Sets the current inventory controller instance so this class can
     * utilize its methods.
     *
     * @param inventoryController the InventoryController instance to set
     */
    public void setInventoryController(
            InventoryController inventoryController) {
        this.inventoryController = inventoryController;
    }

    /**
     * Closes the current stage/window.
     */
    public void closeStage() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    /**
     * Handles the visibility of warning labels based on input fields' contents.
     * Shows nameLabelWarning if nameInput is empty, priceLabelWarning if
     * priceInput is empty,
     * and typeLabelWarning if typeInput is not selected.
     */
    public void handleLabelVisibility() {
        nameLabelWarning.setVisible(nameInput.getText().isEmpty());
        priceLabelWarning.setVisible(priceInput.getText().isEmpty());
        typeLabelWarning.setVisible(typeInput.getSelectionModel().isEmpty());
    }

    /**
     * Handles the logic to add a new product.
     * Validates inputs and adds the product to the database.
     * Shows success or error alerts accordingly.
     */
    public void addProduct() {
        if (validateEmptyInputs()) {
            String productName = nameInput.getText().toUpperCase();
            Integer productPrice = Integer.valueOf(priceInput.getText());
            String productType = typeInput.getValue();

            if (!inventoryDAO.addInventoryToDatabase(productName, productPrice,
                                                     productType)) {
                customAlert.generateAlert("Error adding product",
                                          "A product with the same name " +
                                          "already exists",
                                          Alert.AlertType.ERROR).showAndWait();
            } else {
                Stage stage = (Stage) confirmBtn.getScene().getWindow();
                stage.close();
                customAlert.generateAlert("Product added",
                                          "The product has been added " +
                                          "successfully",
                                          Alert.AlertType.INFORMATION).show();

                inventoryController.loadInventoryData(); // Refresh inventory
            }
        }
    }

    /**
     * Validates if the input fields are empty.
     * Shows an alert if any field is empty.
     *
     * @return true if all inputs are valid; false otherwise
     */
    private boolean validateEmptyInputs() {
        if (nameInput.getText().isEmpty()) {
            customAlert.generateAlert("Error agregando el producto",
                                      "El nombre del producto está vacio",
                                      Alert.AlertType.ERROR).showAndWait();
            return false;
        } else if (priceInput.getText().isEmpty()) {
            customAlert.generateAlert("Error agregando el producto",
                                      "El precio del producto está vacio",
                                      Alert.AlertType.ERROR).showAndWait();
            return false;
        } else if (typeInput.getSelectionModel().isEmpty()) {
            customAlert.generateAlert("Error agregando el producto",
                                      "El tipo del producto está vacio",
                                      Alert.AlertType.ERROR).showAndWait();
            return false;
        }
        return true;
    }


    /**
     * Listens for changes in the priceInput field and restricts input to
     * numeric values only.
     */
    private void changePriceInputType() {
        priceInput.textProperty()
                  .addListener((observable, oldValue, newValue) -> {
                      if (!newValue.matches("\\d*")) {
                          priceInput.setText(newValue.replaceAll("\\D", ""));
                      }
                  });
    }


}
