package com.example.poshaisan;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.awt.print.PrinterException;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Controller class for handling UI interactions related to adding tables
 * and orders.
 */
public class AddOrderController {

    // Fields --------------------------------------------------

    public VBox addOrderSection;
    private static final SidebarController sidebar =
            SidebarController.getInstance();
    private static final Utils utils = new Utils();
    private static final List<String> SERVERS = utils.getSERVERS();
    private static final InventoryDAO inventoryDAO = new InventoryDAO();
    private static final TablesList tables = TablesList.getInstance();
    private static List<InventoryItem> items = null;
    private final Locale chileLocale = Locale.forLanguageTag("es-CL");
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(
            chileLocale);
    private final CustomAlert customAlert = new CustomAlert();
    private final PrinterConnection printer = new PrinterConnection();

    ObservableList<OrderItem> itemsList = FXCollections.observableArrayList();


    @FXML
    Button clearBtn;
    @FXML
    Button backCommandBtn;
    @FXML
    Button backBtn;
    @FXML
    FlowPane flowpaneContainer;
    @FXML
    Button printBtn;
    @FXML
    TextField productInput;
    @FXML
    TableColumn<OrderItem, String> productNameColumn;
    @FXML
    TableColumn<OrderItem, Integer> productQuantityColumn;
    @FXML
    TableView<OrderItem> productTable;
    @FXML
    TableColumn<OrderItem, Integer> productTotalColumn;
    @FXML
    TextField quantityInput;
    @FXML
    TextField tableInput;
    @FXML
    TextField serverInput;
    @FXML
    Button menuBtn;
    @FXML
    TextField discountInput;
    @FXML
    Label subtotalLabel;
    @FXML
    Label totalLabel;
    @FXML
    HBox tableContainer;
    @FXML
    Label OrderLabel;
    @FXML
    Button copyBtn;
    @FXML
    TextField Cambio_one;
    @FXML
    TextField Cambio_two;
    @FXML
    Button AddChange;
    @FXML
    TextField Comment;
    @FXML
    Button Add_Comment;
    @FXML
    TextField Comment_Price;


    TableOrder currentOrder = null;

    // Methods --------------------------------------------------

    /**
     * Changes the FlowPane section to contain details about the product.
     * In this section, the user can change its price, name, and quantity.
     *
     * @param hbox The HBox to extract the TextField
     * @return a TextField if the HBox contains one
     */
    public static TextField getTextFieldFromHBox(HBox hbox) {
        for (Node node : hbox.getChildren()) {
            if (node instanceof TextField) {
                return (TextField) node;
            }
        }
        return null;
    }

    /**
     * Fetches products from Database and hides menu button.
     */
    public void initialize() {
        items = fetchProductNames();
        menuBtn.setVisible(false);
    }

    /**
     * Initializes the controller, setting up input fields and their behaviors.
     *
     * @param order A TableOrder object containing details about a table,
     *              for example, its items, server assigned, discount, etc.
     */
    public void initTableOrder(TableOrder order, Boolean isTable) {
        this.currentOrder = order;
        System.out.println("Este es el pedido " + order.getId().toString());

        Platform.runLater(() -> {
            Scene currentScene = clearBtn.getScene();
            if (currentScene != null) {
                if (!isTable) {
                    OrderLabel.setText("Llevar");
                    System.out.println("Es para llevar");
                }
                else{
                    OrderLabel.setText("Mesa");
                    System.out.println("Es para Mesa");
                }

                initializeQuantityInput();
                initializeServerInput();
                initializeProductInput();
                initializeSearchProductInput(Cambio_one);
                initializeSearchProductInput(Cambio_two);
                initializeTableInput(isTable);
                initializeDiscountInput();
                initTable();
                showTypesUI();
                productTable.setItems(itemsList);
                initProductTable();
                serverInput.setText(order.getServer());
                tableInput.setText(order.getTableName());

                if (currentOrder != null) {
                    updateItemsList();
                }
                /*
                 * Ensure the scene is completely loaded before adding an event
                 * handler
                 */
                Platform.runLater(() -> {
                    if (isTable) {
                        serverInput.requestFocus();
                    } else {
                        productInput.requestFocus();
                    }
                    backCommandBtn.setOnAction(evt -> printOrder_Command(isTable));
                    printBtn.setOnAction(evt -> Add_to_BD(order, isTable) );
                    clearBtn.setOnAction(evt -> Delete_From_BD());
                    copyBtn.setOnAction(evt -> printOrder(isTable, true));

                    currentScene.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.F5) {
                            printOrder(isTable, false);
                        }
                        if (event.getCode() == KeyCode.F8) {
                            printOrder(isTable, true);
                        }
                        if (event.getCode() == KeyCode.F6) {
                            clearTable(isTable);
                        }
                    });
                });
            }
        });
    }

    /**
     * Navigates back to the sales page using the sidebar.
     */
    public void goBack() {

        List<StoredOrder> orders = printer.getOrders_command();
        //System.out.println("la ultima id del pedido es " + orders.get(orders.size()-1).getId().toString());
        boolean flag = true;
        int count = orders.size();
        while (flag && count > 0)
        {
            if(orders.get(count - 1).getId().equals(currentOrder.getId()))
            {
                System.out.println("la ultima id del pedido es " + orders.size());
                System.out.println("la ultima id del pedido es " + orders.get(count-1).getId().toString());
                System.out.println("la ultima id del pedido es " + currentOrder.getId().toString());
                if (OrderLabel.getText().equals("Mesa")) {
                    printer.UpdateOrderToBD(currentOrder, true);
                }

                if(OrderLabel.getText().equals("Llevar")) {
                    printer.UpdateOrderToBD(currentOrder, false);
                }

                flag = false;
            }
            count -= 1;
        }

        if(flag) {


            //System.out.println("la ultima id del pedido es " + orders.get(count-1).getId().toString());
            System.out.println("la ultima id del pedido es " + currentOrder.getId().toString());

            if (OrderLabel.getText().equals("Llevar")) {
                printer.AddOrderToBD(currentOrder, false);
            }

            if (OrderLabel.getText().equals("Mesa")){

                printer.AddOrderToBD(currentOrder, true);
            }

            System.out.println("Se creo una orden");
        }


        sidebar.loadPage("sales", null, false);
        //printer.AddOrderToBD(currentOrder, false);
    }

    /**
     * Navigates back to the menu, showing all menu.
     */
    public void goBackMenu() {
        menuBtn.setVisible(false);
        flowpaneContainer.getChildren().clear();
        showTypesUI();
    }

    /**
     * Deletes a table that is inside tables singleton list.
     */
    public void clearTable(boolean isTable) {
        if (isTable){
            tables.deleteTable(currentOrder);
        } else {
            tables.deleteTakeout(currentOrder);
        }

    }

    public void Delete_From_BD() {

        printer.Delete_from_BD(currentOrder);
        tables.goToSales();
    }

    /**
     * Prints the current order and clears the table.
     *
     * @param isTable A boolean indicating if the order is for a table.
     */
    private void printOrder(boolean isTable, boolean isCopy) {
        try {
            printer.printOrder(currentOrder, isTable, isCopy);
            if (!isCopy){
                clearTable(isTable);

            }
        } catch (PrinterException e) {
            throw new RuntimeException(e);
        }
    }

    public void Add_to_BD(TableOrder order, Boolean isTable){

       printer.Add_to_BD(order, isTable);
       tables.goToSales();

    }

    private void printOrder_Command(boolean isTable) {
        try {
            printer.printCommand(currentOrder, isTable);
        } catch (PrinterException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes the product table view, setting the handlers for every
     * table row.
     */
    private void initProductTable() {
        productTable.setRowFactory(evt -> {
            TableRow<OrderItem> row = new TableRow<>();
            row.setOnMouseClicked(clickEvt -> {
                if (!row.isEmpty()) {
                    String productName = row.getItem().getName();
                    Integer productQuantity = row.getItem().getQuantity();
                    Integer productPrice = row.getItem().getPrice();
                    Integer id = row.getItem().getId();

                    createProductDetailView(productName, productPrice,
                            productQuantity, id);
                }
            });
            return row;
        });
    }

    /**
     * Changes the FlowPane section to contain details about the product.
     * In this section, the user can change its price, name, and quantity.
     *
     * @param productName     the name of the product
     * @param productQuantity the quantity of the product
     * @param productPrice    the price of the product
     */
    private void createProductDetailView(String productName,
                                         Integer productPrice,
                                         Integer productQuantity, Integer id) {
        HBox quantityContainer =
                createProductQuantitySection(productQuantity);
        HBox priceContainer = createProductPriceSection(productPrice,
                productQuantity);
        VBox productViewContainer = new VBox();
        TextField productQuantityInput =
                getTextFieldFromHBox(quantityContainer);

        TextField productPriceInput = getTextFieldFromHBox(priceContainer);
        HBox nameContainer = createProductNameSection(productName, id,
                productQuantityInput);
        final HBox[] actionsContainer = {createProductActionsSection(
                productName, productPrice, id, productQuantityInput,
                productQuantity)};

        productViewContainer.setSpacing(20);
        assert productQuantityInput != null;
        assert productPriceInput != null;

        // Add listeners after initializing the containers, so we can bind them
        addListenersProductView(productQuantityInput, productPriceInput,
                productQuantityInput, productName,
                productQuantity, id);
        addListenersProductView(productQuantityInput, productPriceInput,
                productPriceInput, productName,
                productPrice, id);

        // Add a listener to productPriceInput to update the actions section
        productPriceInput.textProperty()
                .addListener((observable, oldValue, newValue) -> {
                    try {
                        Integer newPrice = Integer.parseInt(newValue);
                        HBox updatedActionsContainer =
                                createProductActionsSection(
                                        productName, newPrice, id,
                                        productQuantityInput,
                                        productQuantity);
                        int index = productViewContainer.getChildren()
                                .indexOf(
                                        actionsContainer[0]);

                        productViewContainer.getChildren()
                                .set(index,
                                        updatedActionsContainer);
                        actionsContainer[0] = updatedActionsContainer;
                    } catch (NumberFormatException e) {
                        // Handle invalid number format if needed
                    }
                });
        flowpaneContainer.getChildren().clear();
        productViewContainer.getChildren().addAll(nameContainer,
                quantityContainer,
                priceContainer,
                actionsContainer[0]);
        flowpaneContainer.getChildren().add(productViewContainer);
        menuBtn.setVisible(true);
    }

    /**
     * Adds listeners to the input fields in the product view.
     *
     * @param quantityInput   The TextField for entering quantity.
     * @param priceInput      The TextField for entering price.
     * @param initializeInput The TextField for initializing input.
     * @param productName     The name of the product being edited.
     * @param initValue       The initial value for the initializeInput field.
     * @param id              The unique identifier of the product.
     */
    private void addListenersProductView(TextField quantityInput,
                                         TextField priceInput,
                                         TextField initializeInput,
                                         String productName,
                                         Integer initValue, Integer id) {
        initializeInput.focusedProperty().addListener((observable, oldValue,
                                                       newValue) -> {
            if (!newValue) {
                if (initializeInput.getText().isEmpty()) {
                    customAlert.generateAlert("Error de formato",
                                    "El campo no puede estar vacio",
                                    Alert.AlertType.ERROR)
                            .showAndWait();
                    initializeInput.setText(initValue.toString());

                } else if (Objects.equals(quantityInput.getText(), "0")) {
                    currentOrder.deleteItem(id);
                    updateItemsList();
                    goBackMenu();
                } else {
                    currentOrder.setItem(productName,
                            Integer.parseInt(
                                    quantityInput.getText()),
                            Integer.parseInt(priceInput.getText()),
                            id);
                    updateItemsList();
                }
            }
        });

        // Accept only Integer values
        initializeInput.textProperty().addListener((observable, oldValue,
                                                    newValue) -> {
            if (!newValue.matches("\\d*")) {
                initializeInput.setText(newValue.replaceAll("\\D", ""));
            }
        });

        initializeInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER ||
                    event.getCode() == KeyCode.TAB) {
                if (initializeInput.getText().isEmpty()) {
                    customAlert.generateAlert("Error de formato",
                                    "El campo no puede estar vacio",
                                    Alert.AlertType.ERROR)
                            .showAndWait();
                    initializeInput.setText(initValue.toString());

                } else if (Objects.equals(quantityInput.getText(), "0")) {
                    currentOrder.deleteItem(id);
                    updateItemsList();
                    goBackMenu();
                } else {
                    currentOrder.setItem(productName,
                            Integer.parseInt(
                                    quantityInput.getText()),
                            Integer.parseInt(priceInput.getText()),
                            id);
                    updateItemsList();
                }
            }
        });

        quantityInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER ||
                    event.getCode() == KeyCode.TAB) {
                priceInput.requestFocus();
            }
        });
    }

    /**
     * Creates an HBox containing two labels, indicating the product name.
     *
     * @param productName the name of the product
     * @return a formatted HBox
     */
    private HBox createProductNameSection(String productName, Integer id,
                                          TextField productQuantityInput) {
        Label defaultProductNameLabel = new Label("Producto: ");
        defaultProductNameLabel.getStyleClass()
                .add("product-view-input-label");
        HBox nameContainer = createProductViewHBox();

        // If the id is >= 10000, it means it's an off-menu item
        if (id >= 10000) {
            TextField productNameInput = createProductNameInput(productName, id,
                    productQuantityInput);
            productNameInput.requestFocus();
            nameContainer.getChildren().addAll(defaultProductNameLabel,
                    productNameInput);
        } else {
            Label productNameLabel = new Label(productName);
            productNameLabel.getStyleClass().add("input-label");
            nameContainer.getChildren()
                    .addAll(defaultProductNameLabel, productNameLabel);
        }
        return nameContainer;
    }

    private TextField createProductNameInput(String productName, Integer id,
                                             TextField productQuantityInput) {
        TextField productNameInput = new TextField();
        productNameInput.setPrefSize(300, 51.2);
        productNameInput.setMinSize(300, 51.2);
        productNameInput.setMaxSize(300, 51.2);
        productNameInput.setText(productName);

        productNameInput.focusedProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        currentOrder.setProductName(
                                productNameInput.getText(), id);
                        updateItemsList();
                    }
                });

        productNameInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER ||
                    event.getCode() == KeyCode.TAB) {
                currentOrder.setProductName(productNameInput.getText(), id);
                productQuantityInput.requestFocus();
                updateItemsList();
            }
        });

        Platform.runLater(() -> {
            productNameInput.requestFocus();
            productNameInput.selectAll();
        });

        return productNameInput;
    }

    /**
     * Creates an HBox containing one Label and one TextField, indicating the
     * product quantity
     *
     * @param productQuantity the quantity of the product
     * @return a formatted HBox
     */
    private HBox createProductQuantitySection(Integer productQuantity) {
        TextField productQuantityInput =
                initializeProductViewQuantity(productQuantity);
        productQuantityInput.setId("product-quantity-input");

        Label productQuantityLabel = new Label("Cantidad: ");
        HBox quantityContainer = createProductViewHBox();

        productQuantityLabel.getStyleClass().add("product-view-input-label");
        quantityContainer.getChildren().addAll(productQuantityLabel,
                productQuantityInput);

        return quantityContainer;
    }

    /**
     * Initializes the product quantity input inside the product view section.
     *
     * @param productQuantity the quantity of the product
     * @return a formatted and initialized product quantity textfield.
     */
    private TextField initializeProductViewQuantity(Integer productQuantity) {
        TextField productQuantityInput = new TextField();

        productQuantityInput.setPrefSize(69.6, 51.2);
        productQuantityInput.setMinSize(69.6, 51.2);
        productQuantityInput.setMaxSize(69.6, 51.2);
        productQuantityInput.setText(productQuantity.toString());
        productQuantityInput.getStyleClass().add("table-input");

        return productQuantityInput;
    }

    /**
     * Creates an HBox containing one Label and one TextField, indicating the
     * product price
     *
     * @param productPrice    the price of the product
     * @param productQuantity the quantity of the product
     * @return a formatted HBox
     */
    private HBox createProductPriceSection(Integer productPrice,
                                           Integer productQuantity) {
        TextField productPriceInput =
                initializeProductViewPrice(productPrice, productQuantity);
        Label productPriceLabel = new Label("Precio unitario: ");
        HBox priceContainer = createProductViewHBox();

        productPriceLabel.getStyleClass().add("product-view-input-label");
        priceContainer.getChildren().addAll(productPriceLabel,
                productPriceInput);
        return priceContainer;
    }

    /**
     * Initializes the product price input inside the product view section.
     *
     * @param productPrice    the price of the product
     * @param productQuantity the quantity of the product
     * @return a formatted and initialized product price textfield.
     */
    private TextField initializeProductViewPrice(Integer productPrice,
                                                 Integer productQuantity) {
        TextField productPriceInput = new TextField();

        productPriceInput.setId("product-price-input");
        productPriceInput.setText(
                String.valueOf((productPrice / productQuantity)));
        productPriceInput.getStyleClass().add("table-input");

        return productPriceInput;
    }

    /**
     * Creates an HBox containing three buttons: one to increment the product
     * quantity, one to reduce the product quantity, and one to delete the
     * product.
     *
     * @param productName          the name of the product
     * @param productPrice         the price of the product
     * @param id                   the id of the product
     * @param productQuantityInput the TextField of the quantity of a product
     * @param initialQuantity      the initial quantity of the product
     * @return a formatted HBox
     */
    private HBox createProductActionsSection(String productName,
                                             Integer productPrice, Integer id,
                                             TextField productQuantityInput,
                                             Integer initialQuantity) {
        HBox actionsContainer = createProductViewHBox();
        Button incrementButton = createActionButton("Agregar", productName,
                productPrice, id,
                productQuantityInput,
                initialQuantity);

        incrementButton.setId("product-add-btn");

        Button reduceButton = createActionButton("Disminuir", productName,
                productPrice, id,
                productQuantityInput,
                initialQuantity);

        reduceButton.setId("product-reduce-btn");

        Button deleteButton = createActionButton("Eliminar", productName,
                productPrice, id,
                productQuantityInput,
                initialQuantity);

        deleteButton.setId("product-delete-btn");

        actionsContainer.getChildren().addAll(incrementButton, reduceButton,
                deleteButton);
        return actionsContainer;
    }

    /**
     * Creates a Button which action depends on the type of text inside of it.
     * It handles the increment, reduction, and deletion of a specified item.
     *
     * @param text                 the text that will be shown on the button
     * @param productName          the name of the product
     * @param price                the price of the product
     * @param id                   the id of the product
     * @param productQuantityInput the TextField of the product quantity
     * @param initialQuantity      the initial quantity of the product
     * @return a formatted and initialized button
     */
    private Button createActionButton(String text, String productName,
                                      Integer price, Integer id,
                                      TextField productQuantityInput,
                                      Integer initialQuantity) {
        Button actionButton = new Button(text);

        actionButton.getStyleClass().add("btn");

        switch (text) {
            case "Agregar":
                actionButton.getStyleClass().add("green-background");
                actionButton.setOnMouseClicked(event -> {
                    Integer newQuantity = currentOrder.addItem(productName, 1,
                            price, id,
                            initialQuantity);
                    updateItemsList();
                    productQuantityInput.setText(newQuantity.toString());
                });
                break;

            case "Disminuir":
                actionButton.getStyleClass().add("yellow-background");
                actionButton.setOnMouseClicked(event -> {
                    Integer newQuantity = currentOrder.reduceItem(id, price,
                            initialQuantity);
                    productQuantityInput.setText(newQuantity.toString());
                    updateItemsList();
                    if (Objects.equals(0, newQuantity)) {
                        goBackMenu();
                    }
                });
                break;

            case "Eliminar":
                actionButton.getStyleClass().add("red-background");
                actionButton.setOnMouseClicked(event -> {
                    currentOrder.deleteItem(id);
                    updateItemsList();
                    goBackMenu();
                });
                break;

            default:
                break;
        }
        return actionButton;
    }

    /**
     * Creates an HBox.
     *
     * @return a formatted HBox
     */
    private HBox createProductViewHBox() {
        HBox productViewHBox = new HBox();

        productViewHBox.setAlignment(Pos.CENTER_LEFT);
        productViewHBox.setSpacing(10);

        return productViewHBox;
    }

    /**
     * Updates TableView items and updates subtotal and total labels.
     */
    private void updateItemsList() {
        itemsList.clear();
        itemsList.addAll(currentOrder.getItems());
        updatePriceLabels();
    }

    /**
     * Updates total and subtotal labels. It utilizes NumberFormat instance
     * to format the price to a Chilean format.
     */
    private void updatePriceLabels() {
        Integer orderSum = currentOrder.getItemsSum();
        Integer discount = 0;

        if (!discountInput.getText().isEmpty()) {
            discount = Integer.parseInt(discountInput.getText());
        }

        String formattedSubTotal =
                numberFormat.format(orderSum);
        String formattedTotal =
                numberFormat.format(orderSum - discount);

        subtotalLabel.setText(formattedSubTotal);
        totalLabel.setText(formattedTotal);
    }

    /**
     * Initializes the TableView that contains the products, quantities, and
     * their prices.
     */
    private void initTable() {
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>(
                "name"));
        productQuantityColumn.setCellValueFactory(new PropertyValueFactory<>(
                "quantity"));
        productTotalColumn.setCellValueFactory(new PropertyValueFactory<>(
                "price"));
    }

    /**
     * Initializes the table input field to move focus to the product input
     * on Enter or Tab.
     */
    private void initializeTableInput(boolean isTable) {
        final boolean[] firstFocusLost = {false};

        tableInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER ||
                    event.getCode() == KeyCode.TAB) {
                Platform.runLater(() -> {
                    if (productInput != null) {
                        productInput.requestFocus();
                    }
                });
                event.consume();
            }
        });

        tableInput.focusedProperty().addListener((observable, oldValue,
                                                  newValue) -> {
            if (firstFocusLost[0] && !newValue) {
                if (isTable){
                    if (!tables.checkDuplicatesTables(tableInput.getText(),
                            currentOrder)) {
                        currentOrder.setTableName(tableInput.getText());
                    } else {
                        customAlert.generateAlert("Error de nombre",
                                        "La mesa ya existe",
                                        Alert.AlertType.ERROR)
                                .showAndWait();
                        tableInput.setText("");
                        tableInput.requestFocus();
                    }
                } else {
                    if (!tables.checkDuplicatesTakeouts(tableInput.getText(),
                            currentOrder)) {
                        currentOrder.setTableName(tableInput.getText());
                    } else {
                        customAlert.generateAlert("Error de nombre",
                                        "El para llevar ya existe",
                                        Alert.AlertType.ERROR)
                                .showAndWait();
                        tableInput.setText("");
                        tableInput.requestFocus();
                    }
                }
            }
            if (newValue) {
                firstFocusLost[0] = true;
            }
        });
    }

    public void AddComment(){

        String Comentario = Comment.getText();
        try{
            Integer Price = Integer.parseInt(Comment_Price.getText());
            String Change = "";

            for (Integer i = 0; i < Comentario.length() && i < 28; i++) {

                Change += Comentario.charAt(i);
            }

            Integer id_comment = createItemId();
            if(Price >= 0 && !Comentario.isEmpty()) {
                currentOrder.addItem(Change, 1, Price, id_comment, 1);
                Comment_Price.setText("0");
                Comment.setText("");
                updateItemsList();
            }
        }
        catch (NumberFormatException e){
            Comment_Price.setText("0");
            Comment.setText("");
        }
        System.out.println("SE ANADE EL COMENTARIO");
    }



    public void DoChange(){

        String Change_one = Cambio_one.getText();
        String Change_two = Cambio_two.getText();

        Integer productPrice_one = getItemPrice(Change_one);
        Integer productPrice_two = getItemPrice(Change_two);

        if(!Change_one.isEmpty() && !Change_two.isEmpty()){

            Integer newPrice = productPrice_two - productPrice_one;


            String Change = "";

            for (Integer i = 0; i < Change_one.length() && i < 14; i++) {

                Change += Change_one.charAt(i);
            }

            Change += " X ";

            for (Integer i = 0; i < Change_two.length() && i < 14; i++) {

                Change += Change_two.charAt(i);
            }

            Integer id_product = createItemId();
            System.out.println("El cambio " + Change);
            System.out.println("id del producto " + id_product);
            System.out.println("La diferencia " + newPrice);

            if (newPrice <= 0) {
                currentOrder.addItem(Change,1,0, id_product, 1);

            } else {

                currentOrder.addItem(Change, 1, newPrice, id_product, 1);
            }

            updateItemsList();
            Cambio_one.setText("");
            Cambio_two.setText("");

        }
        System.out.println("ESTA Haciendose el cambio");
    }

    /**
     * Listens for changes in the Quantity field and restricts input to
     * numeric values only.
     */
    private void initializeQuantityInput() {
        // Accept only Integer values
        quantityInput.textProperty().addListener((observable, oldValue,
                                                  newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantityInput.setText(newValue.replaceAll("\\D", ""));
            }
        });

        quantityInput.setOnKeyReleased(event -> {
            String productName = productInput.getText();
            String productQuantity = quantityInput.getText();

            if (!productQuantity.isEmpty()) {
                if (event.getCode() == KeyCode.ENTER) {
                    if (!productName.isEmpty()) {
                        Integer integerProductQuantity =
                                Integer.parseInt(productQuantity);
                        Integer productPrice = getItemPrice(productName);

                        if (productName.equals("PRODUCTO FUERA DE MENÚ")) {
                            Integer id = createItemId();

                            createProductDetailView(productName, 0,
                                    integerProductQuantity, id);

                            currentOrder.addItem(productName,
                                    integerProductQuantity,
                                    productPrice, id, 1);
                        } else {
                            currentOrder.addItem(productName,
                                    integerProductQuantity,
                                    productPrice,
                                    getItemId(productName), 1);
                        }

                        /*
                         * Use updateItemsList because tableview is not
                         * updated automatically
                         */
                        updateItemsList();
                        productInput.clear();
                        quantityInput.clear();
                        productInput.requestFocus();
                    }

                    quantityInput.setText("1");
                }
            }
        });
    }

    /**
     * Listens for changes in the Discount field and restricts input to
     * numeric values only.
     */
    private void initializeDiscountInput() {
        discountInput.textProperty().addListener((observable, oldValue,
                                                  newValue) -> {
            if (!newValue.matches("\\d*")) {
                discountInput.setText(newValue.replaceAll("\\D", ""));
            }
        });

        discountInput.focusedProperty().addListener((observable, oldValue,
                                                     newValue) -> {
            if (!newValue) {
                if (!discountInput.getText().isEmpty()) {
                    currentOrder.setDiscount(Integer.parseInt(
                            discountInput.getText()));
                } else {
                    currentOrder.setDiscount(0);
                }
                updatePriceLabels();
            }
        });

        discountInput.setOnKeyReleased(event -> {
            // If the field is empty then the default discount value should be 0
            if (!discountInput.getText().isEmpty()) {
                currentOrder.setDiscount(
                        Integer.parseInt(discountInput.getText()));
            } else {
                currentOrder.setDiscount(0);
            }
            updatePriceLabels();
        });

        discountInput.setText(currentOrder.getDiscount().toString());
    }

    /**
     * Get specific item price.
     *
     * @param productName The name of the product
     * @return The price of the given product
     */
    private Integer getItemPrice(String productName) {
        for (InventoryItem item : items) {
            if (Objects.equals(item.getItemName(), productName)) {
                return item.getItemPrice();
            }
        }
        return 0;
    }

    /**
     * Get specific item id.
     *
     * @param productName The name of the product
     * @return The id of the given product
     */
    private Integer getItemId(String productName) {
        for (InventoryItem item : items) {
            if (Objects.equals(item.getItemName(), productName)) {
                return item.getItemId();
            }
        }
        return 0;
    }

    /**
     * Create an id for an off-menu product.
     *
     * @return the created id
     */
    private Integer createItemId() {
        Integer maxId = currentOrder.getMaxId();
        return Math.max(maxId + 1, 10000);
    }

    /**
     * Initializes the server input dropdown with predefined server options.
     */
    private void initializeServerInput() {
        AutoCompletionBinding<String> serverAutoComplete =
                TextFields.bindAutoCompletion(serverInput, SERVERS);
        serverAutoComplete.setDelay(50);

        serverAutoComplete.setOnAutoCompleted(
                event -> tableInput.requestFocus());

        serverInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                tableInput.requestFocus();
            }
        });

        serverInput.focusedProperty().addListener((observable, oldValue,
                                                   newValue) -> {
            if (!newValue) {
                currentOrder.setServer(serverInput.getText());
            }
        });
    }

    /**
     * Fetches product names from the inventory to provide suggestions in the
     * product input field.
     *
     * @return List of product names fetched from the inventory.
     */
    private List<InventoryItem> fetchProductNames() {
        return inventoryDAO.fetchInventoryFromDatabase();
    }

    /**
     * Initializes the product input field with auto-completion based on
     * fetched product names.
     */
    private void initializeProductInput() {
        List<String> products =
                new java.util.ArrayList<>(items.stream()
                        .map(InventoryItem::getItemName)
                        .toList());
        products.add("PRODUCTO FUERA DE MENÚ");
        AutoCompletionBinding<String> autoCompletionBinding =
                TextFields.bindAutoCompletion(productInput, products);
        autoCompletionBinding.setDelay(50);

        productInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleEnterKeyPress(productInput, products);
            }
        });

        productInput.focusedProperty().addListener((observable, oldValue,
                                                    newValue) -> {
            if (!newValue) {
                handleFocusLost(productInput, products);
            }
        });

        autoCompletionBinding.setOnAutoCompleted(
                event -> quantityInput.requestFocus());
    }

    private void initializeSearchProductInput(TextField textfield) {
        List<String> products =
                new java.util.ArrayList<>(items.stream()
                        .map(InventoryItem::getItemName)
                        .toList());
        products.add("PRODUCTO FUERA DE MENÚ");
        AutoCompletionBinding<String> autoCompletionBinding =
                TextFields.bindAutoCompletion(textfield, products);
        autoCompletionBinding.setDelay(50);


        textfield.focusedProperty().addListener((observable, oldValue,
                                                    newValue) -> {
            if (!newValue) {
                handleFocusLost(textfield, products);
            }
        });

        autoCompletionBinding.setOnAutoCompleted(
                event -> quantityInput.requestFocus());
    }



    /**
     * Handles Enter key press in the product input field to clear if the
     * input is not a valid product.
     *
     * @param textField The TextField where the Enter key was pressed.
     * @param products  List of valid product names for comparison.
     */
    private void handleEnterKeyPress(TextField textField,
                                     List<String> products) {
        String input = textField.getText().trim();
        if (!products.contains(input)) {
            textField.clear();
        }
    }

    /**
     * Handles focus lost from the product input field to clear if the input
     * is not a valid product.
     *
     * @param textField The TextField where the focus was lost.
     * @param products  List of valid product names for comparison.
     */
    private void handleFocusLost(TextField textField, List<String> products) {
        String input = textField.getText().trim();
        if (!products.contains(input)) {
            textField.clear();
        }
    }

    /**
     * Show boxes on the UI containing all the dish types.
     */
    private void showTypesUI() {
        for (String dishType : utils.getDISH_TYPES()) {
            VBox vbox = createVBox();
            Label label = createDishTypeLabel(dishType);
            List<InventoryItem> filteredDishes =
                    items.stream()
                            .filter(item -> item.getItemType()
                                    .equals(dishType))
                            .toList();

            vbox.getChildren().addAll(label);

            vbox.setOnMouseClicked(event -> {
                menuBtn.setVisible(true);
                flowpaneContainer.getChildren().clear();
                showDishesUI(filteredDishes);
            });

            flowpaneContainer.getChildren().add(vbox);
        }
    }

    /**
     * Show boxes on the UI containing the dish name and its price.
     */
    private void showDishesUI(List<InventoryItem> filteredDishes) {
        for (InventoryItem item : filteredDishes) {
            VBox newVBox = createVBox();
            Label newLabel = createDishTypeLabel(item.getItemName());

            newVBox.getChildren().addAll(newLabel);

            newVBox.setOnMouseClicked(evt -> {
                currentOrder.addItem(item.getItemName(), 1,
                        item.getItemPrice(),
                        getItemId(item.getItemName()), 1);
                updateItemsList();
            });

            flowpaneContainer.getChildren().add(newVBox);
        }
    }

    /**
     * Creates a dish VBox type container.
     */
    private VBox createVBox() {
        VBox vbox = new VBox();
        vbox.setPrefSize(115, 115);
        vbox.getStyleClass().add("dish-container");
        return vbox;
    }

    /**
     * Create a dish Label type.
     */
    private Label createDishTypeLabel(String name) {
        Label label = new Label(name);
        label.setWrapText(true);
        label.getStyleClass().add("dish-center-label");
        return label;
    }



}