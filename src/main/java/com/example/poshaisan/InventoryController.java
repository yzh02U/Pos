package com.example.poshaisan;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.util.List;
import java.util.logging.Logger;

/**
 * Controller class for the inventory management view.
 */
public class InventoryController {

    // Fields --------------------------------------------------
    public VBox inventorySection;
    private static final Logger logger = Logger.getLogger(
            InventoryController.class.getName());
    private static final String EDIT_PRODUCT_TITLE = "Edición de producto";
    private static final String EDIT_PRODUCT_SUCCESS = "Producto modificado " +
                                                       "exitosamente";
    private static final String EDIT_PRODUCT_ERROR = "Hubo un error " +
                                                     "modificando el producto";
    private final Utils utils = new Utils();
    private final List<String> DISH_TYPES = utils.getDISH_TYPES();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final CustomAlert customAlert = new CustomAlert();
    private final ObservableList<InventoryItem> inventoryData =
            FXCollections.observableArrayList();
    private FilteredList<InventoryItem> filteredList;

    @FXML
    TableView<InventoryItem> inventoryTable;
    @FXML
    TableColumn<InventoryItem, String> itemNameColumn;
    @FXML
    TableColumn<InventoryItem, Integer> itemPriceColumn;
    @FXML
    TableColumn<InventoryItem, String> itemDeleteColumn;
    @FXML
    TableColumn<InventoryItem, String> itemTypeColumn;
    @FXML
    TextField filterInput;
    @FXML
    Button addProductBtn;

    // Methods --------------------------------------------------

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     */
    public void initialize() {
        try {
            inventoryTable.setEditable(true);
            initializeItemNameColumn();
            initializeItemPriceColumn();
            initializeItemTypeColumn();
            loadInventoryData();
            setupSearchFunctionality();
        } catch (Exception e) {
            logger.severe(e.toString());
        }

        Callback<TableColumn<InventoryItem, String>, TableCell<InventoryItem,
                String>> cellFactory = param -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    FontAwesomeIconView deleteIcon = new FontAwesomeIconView(
                            FontAwesomeIcon.TRASH);
                    HBox manageBtn = new HBox(deleteIcon);

                    deleteIcon.setOnMouseClicked(
                            event -> handleDeleteIconClick());

                    manageBtn.setStyle("-fx-alignment:center");

                    setGraphic(manageBtn);
                    setText(null);
                }
            }
        };

        itemDeleteColumn.setCellFactory(cellFactory);
        inventoryTable.setItems(inventoryData);
    }

    /**
     * Shows the add product view.
     */
    public void showAddProductView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("add-product.fxml"));
            Parent parent = loader.load();
            AddProductController addProductController = loader.getController();

            addProductController.setInventoryController(this);

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setTitle("Add Product");
            stage.setScene(scene);
            stage.initStyle(StageStyle.UTILITY);
            stage.showAndWait();
        } catch (Exception e) {
            logger.severe(
                    "Hubo un problema abriendo la pestaña de agregar producto");
            logger.severe(e.getMessage());
            logger.severe(e.toString());
        }
    }


    /**
     * Loads inventory data from the database.
     */
    public void loadInventoryData() {
        try {
            inventoryData.clear();
            List<InventoryItem> items =
                    inventoryDAO.fetchInventoryFromDatabase();
            inventoryData.addAll(items);
        } catch (Exception e) {
            logger.severe(e.toString());
        }
        updateSortedList();
    }

    /**
     * Handles the delete icon click event.
     */
    private void handleDeleteIconClick() {
        InventoryItem selectedItem = inventoryTable.getSelectionModel()
                                                   .getSelectedItem();
        Integer id = selectedItem.getItemId();

        if (inventoryDAO.deleteInventoryFromDatabase(id)) {
            customAlert.generateAlert("Eliminar producto",
                                      "Se ha eliminado el producto " +
                                      "exitosamente",
                                      Alert.AlertType.INFORMATION)
                       .showAndWait();
        } else {
            customAlert.generateAlert("Eliminar producto",
                                      "Hubo un problema eliminando el producto",
                                      Alert.AlertType.ERROR).showAndWait();
        }
        loadInventoryData();
    }

    /**
     * Initializes the item name column.
     */
    private void initializeItemNameColumn() {
        itemNameColumn.setCellValueFactory(
                cellData -> cellData.getValue().itemNameProperty());
        itemNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        itemNameColumn.setOnEditCommit(this::handleItemNameEdit);
    }

    /**
     * Handles the edit commit event for the item name column.
     *
     * @param event the edit commit event
     */
    private void handleItemNameEdit(
            TableColumn.CellEditEvent<InventoryItem, String> event) {
        InventoryItem item = event.getRowValue();
        String newName = event.getNewValue().toUpperCase();

        if (inventoryDAO.editProductNameFromDatabase(item.getItemId(),
                                                     newName)) {
            item.setItemName(newName);
            customAlert.generateAlert(EDIT_PRODUCT_TITLE, EDIT_PRODUCT_SUCCESS,
                                      Alert.AlertType.INFORMATION)
                       .showAndWait();
        } else {
            item.setItemName(event.getOldValue());
            customAlert.generateAlert(EDIT_PRODUCT_TITLE, EDIT_PRODUCT_ERROR,
                                      Alert.AlertType.ERROR).showAndWait();
        }
        event.getTableView().refresh();
    }

    /**
     * Initializes the item price column.
     */
    private void initializeItemPriceColumn() {
        itemPriceColumn.setCellValueFactory(
                cellData -> cellData.getValue().itemPriceProperty().asObject());
        itemPriceColumn.setCellFactory(column -> new IntegerEditingCell());
        itemPriceColumn.setOnEditCommit(this::handleItemPriceEdit);
    }

    /**
     * Handles the edit commit event for the item price column.
     *
     * @param event the edit commit event
     */
    private void handleItemPriceEdit(
            TableColumn.CellEditEvent<InventoryItem, Integer> event) {
        InventoryItem item = event.getRowValue();
        Integer newPrice = event.getNewValue();

        if (inventoryDAO.editProductPriceFromDatabase(item.getItemId(),
                                                      newPrice)) {
            item.setItemPrice(newPrice);
            customAlert.generateAlert(EDIT_PRODUCT_TITLE, EDIT_PRODUCT_SUCCESS,
                                      Alert.AlertType.INFORMATION)
                       .showAndWait();
        } else {
            item.setItemPrice(event.getOldValue());
            customAlert.generateAlert(EDIT_PRODUCT_TITLE, EDIT_PRODUCT_ERROR,
                                      Alert.AlertType.ERROR).showAndWait();
        }
        event.getTableView().refresh();
    }

    /**
     * Initializes the item type column.
     */
    private void initializeItemTypeColumn() {
        itemTypeColumn.setCellValueFactory(
                cellData -> cellData.getValue().itemTypeProperty());
        ObservableList<String> dishTypes = FXCollections.observableArrayList(
                DISH_TYPES);

        itemTypeColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(dishTypes));
        itemTypeColumn.setOnEditCommit(this::handleItemTypeEdit);
    }

    /**
     * Handles the edit commit event for the item type column.
     *
     * @param event the edit commit event
     */
    private void handleItemTypeEdit(
            TableColumn.CellEditEvent<InventoryItem, String> event) {
        InventoryItem item = event.getRowValue();
        String newType = event.getNewValue();

        if (inventoryDAO.editProductTypeFromDatabase(item.getItemId(),
                                                     newType)) {
            item.setItemType(newType);
            customAlert.generateAlert(EDIT_PRODUCT_TITLE, EDIT_PRODUCT_SUCCESS,
                                      Alert.AlertType.INFORMATION)
                       .showAndWait();
        } else {
            item.setItemType(event.getOldValue());
            customAlert.generateAlert(EDIT_PRODUCT_TITLE, EDIT_PRODUCT_ERROR,
                                      Alert.AlertType.ERROR).showAndWait();
        }
        event.getTableView().refresh();
    }



    /**
     * Sets up the search functionality for the inventory table.
     */
    private void setupSearchFunctionality() {
        filteredList = new FilteredList<>(inventoryData, b -> true);

        filterInput.textProperty()
                   .addListener((observable, oldValue, newValue) -> {
                       filteredList.setPredicate(inventoryProduct -> {
                           if (newValue == null || newValue.isEmpty()) {
                               return true;
                           }
                           String lowerCaseFilter = newValue.toLowerCase();
                           if (inventoryProduct.getItemName().toLowerCase()
                                               .contains(lowerCaseFilter)) {
                               return true;
                           } else {
                               return String.valueOf(
                                                    inventoryProduct.getItemPrice())
                                            .contains(lowerCaseFilter);
                           }
                       });
                       updateSortedList();
                   });
        updateSortedList();
    }

    /**
     * Updates the sorted list to reflect changes in the filtered list.
     */
    private void updateSortedList() {
        if (filteredList == null) {
            filteredList = new FilteredList<>(inventoryData, b -> true);
        }

        SortedList<InventoryItem> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty()
                  .bind(inventoryTable.comparatorProperty());
        inventoryTable.setItems(sortedList);
    }
}
