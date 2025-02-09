package com.example.poshaisan;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.*;

/**
 * Controller class for managing sales-related actions and UI interactions.
 */
public class SalesController {

    // Fields --------------------------------------------------
    public List<StoredOrder> Orders;
    public Tab tablesTab;
    public VBox salesSection;
    public Label headerTitle;
    public Button addTableBtn;
    public Button addOrderBtn;
    public Tab ordersTab;
    private static final SidebarController sidebar =
            SidebarController.getInstance();
    private static final Utils utils = new Utils();
    private final ObservableList<TableOrder> tables = TablesList.getInstance()
                                                                .getTablesList();
    private final ObservableList<TableOrder> takeouts = TablesList.getInstance()
                                                                .getTakeoutsList();
    /*
    @FXML
    AnchorPane TableUI;
*/
    @FXML
    FlowPane tablesFlowPane;
    @FXML
    FlowPane takeoutsFlowPane;


    // Methods --------------------------------------------------

    /**
     * Initializes the SalesController.
     * Sets up initial labels for existing TableOrder objects and listens for
     * changes in the tables list.
     */
    public void initialize() {
        // Display initial labels for existing TableOrder objects

        AddOrderDAO get_order = new AddOrderDAO(utils.DB_URL, utils.DB_USER, utils.DB_PASSWORD);
        Orders = get_order.fetchOrdersFromDatabase_Command();
        tables.clear();
        takeouts.clear();

        Orders.forEach(orden -> {


            ObservableList<OrderItem> Item = FXCollections.observableArrayList();
            orden.getItems().forEach(items -> {

                OrderItem orderItem = new OrderItem(items.getName(), items.getPrice(), items.getQuantity(), items.getId());
                Item.add(orderItem);

            });

            if (orden.getIsTable()) {
                TableOrder Temp_Order = new TableOrder(orden.getId(), Item, orden.getStartDateTime(), orden.getServer(), orden.getTableName());
                tables.add(Temp_Order);

            } else {

                TableOrder Temp_Order = new TableOrder(orden.getId(), Item, orden.getStartDateTime(), orden.getServer(), orden.getTableName());
                takeouts.add(Temp_Order);

            }

        });

        tables.forEach(order -> tablesFlowPane.getChildren()
                .add(createTableUI(order)));

        takeouts.forEach(order -> takeoutsFlowPane.getChildren()
                .add(createTakeoutUI(order)));

/*
        TableUI.setPrefSize(600, 400);

        VBox caja = createVBoxUI();
        caja.setLayoutX(0);
        caja.setLayoutY(0);
        TableUI.getChildren().add(caja);

 */
    }

    /**
     * Loads an order with an empty list of items and the current date and
     * time in the Chilean time zone.
     * Creates a TableOrder object and loads the "add-order" page in the
     * sidebar.
     */
    public void loadOrder() {
        PrinterConnection print = new PrinterConnection();

        TableOrder newOrder = createTakeoutOrder();
        takeouts.add(newOrder);
        sidebar.loadPage("add-order", newOrder, false);
    }



    public TableOrder createTakeoutOrder() {

        PrinterConnection print = new PrinterConnection();
        List<StoredOrder> orders = print.getOrders();
        List<StoredOrder> orders_dos = print.getOrders_command();
        int size = orders.size() + orders_dos.size();

        /*
        return new TableOrder(getLatestTakeoutId(),
                              FXCollections.observableArrayList(),
                              utils.getDateTime());
         */

        return new TableOrder(size,
                FXCollections.observableArrayList(),
                utils.getDateTime());

    }
    public Integer getLatestTakeoutId() {
        return takeouts.isEmpty() ? 1 : takeouts.getLast().getId() + 1;
    }

    /**
     * Retrieves the latest table ID from the tables list.
     *
     * @return The latest table ID.
     */
    public Integer getLatestTableId() {
        return tables.isEmpty() ? 1 : tables.getLast().getId() + 1;
    }


    /**
     * Creates a new TableOrder with a new ID and current timestamp.
     *
     * @return The newly created TableOrder object.
     */
    public TableOrder createTableOrder() {


        PrinterConnection print = new PrinterConnection();
        List<StoredOrder> orders = print.getOrders();
        List<StoredOrder> orders_dos = print.getOrders_command();
        int size = orders.size() + orders_dos.size();

        return new TableOrder(size,
                              FXCollections.observableArrayList(),
                              utils.getDateTime());

        /*return new TableOrder(getLatestTakeoutId(),
                              FXCollections.observableArrayList(),
                              utils.getDateTime());  */
    }

    /**
     * Simulates adding a new order to the tables list.
     * Creates a new TableOrder object and adds it to the tables list.
     * Loads the "add-order" page in the sidebar with the new TableOrder.
     */
    public void showAddOrder() {
        TableOrder newOrder = createTableOrder();

        tables.add(newOrder); // Add the new order to the tables list
        sidebar.loadPage("add-order", newOrder, true);
    }

    /**
     * Creates a styled VBox for displaying table information.
     *
     * @return A styled VBox for displaying table information.
     */
    private VBox createVBoxUI() {
        VBox vbox = new VBox();

        vbox.setPrefSize(100, 100);
        vbox.getStyleClass().add("table-container");
        vbox.setPadding(new Insets(20, 10, 20, 10));
        return vbox;
    }

    /**
     * Creates a Label UI component for displaying the table name.
     *
     * @param order The TableOrder object containing the table name.
     * @return A Label component displaying the table name.
     */
    private Label createTableLabelUI(TableOrder order) {
        Label tableName = new Label(order.getTableName());

        tableName.getStyleClass().add("table-name-label");
        return tableName;
    }

    /**
     * Creates a Label UI component for displaying the server assigned to the
     * table.
     *
     * @param order The TableOrder object containing the server name.
     * @return A Label component displaying the server name.
     */
    private Label createTableServerUI(TableOrder order) {
        Label server = new Label(order.getServer());

        server.getStyleClass().add("table-server-label");
        return server;
    }

    /**
     * Creates a VBox UI for displaying a TableOrder.
     *
     * @param order The TableOrder object to display.
     * @return A VBox containing UI elements for the TableOrder.
     */
    private VBox createTableUI(TableOrder order) {
        VBox vbox = createVBoxUI();

        vbox.getChildren()
            .addAll(createTableLabelUI(order), createTableServerUI(order));
        vbox.setOnMouseClicked(
                event -> sidebar.loadPage("add-order", order, true));
        return vbox;
    }

    private VBox createTakeoutUI(TableOrder order) {
        VBox vbox = createVBoxUI();

        vbox.getChildren()
            .addAll(createTableLabelUI(order), createTableServerUI(order));
        vbox.setOnMouseClicked(
                event -> sidebar.loadPage("add-order", order, false));
        return vbox;
    }




}
