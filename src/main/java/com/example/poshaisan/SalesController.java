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
import javafx.scene.shape.Circle;

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
    public Pane MesasaOrg;
    public Pane Terraza;
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


        initializeMainLobby();
        initializeTerrace();
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
    private VBox createVBoxUI(int x, int y) {
        VBox vbox = new VBox();

        vbox.setPrefSize(x, y);
        vbox.getStyleClass().add("table-container");
        vbox.setPadding(new Insets(20, 10, 20, 10));
        return vbox;
    }

    private Pane createCircleUI(){
        Pane circularPane = new Pane();
        circularPane.setPrefSize(100, 100); // Contenedor cuadrado
        // Asigna un fondo para poder visualizar el área recortada
        circularPane.getStyleClass().add("circle-container");
        Circle clipCircle = new Circle(50, 50, 50); // Centro en (100,100) y radio 100
        circularPane.setClip(clipCircle);

        return circularPane;

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


    private Label createTableLabelCircularUI(TableOrder order) {
        Label tableName = new Label(order.getTableName());
        tableName.setLayoutX(40); // Posición X
        tableName.setLayoutY(40);  // Posición Y

        tableName.getStyleClass().add("table-name-circular-label");
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

    private Label createTableServerCircularUI(TableOrder order) {
        Label server = new Label(order.getServer());
        server.setLayoutX(25); // Posición X
        server.setLayoutY(10);  // Posición Y

        server.getStyleClass().add("table-server-circular-label");
        return server;
    }

    /**
     * Creates a VBox UI for displaying a TableOrder.
     *
     * @param order The TableOrder object to display.
     * @return A VBox containing UI elements for the TableOrder.
     */
    private VBox createTableUI(TableOrder order) {
        VBox vbox = createVBoxUI(100, 100);

        vbox.getChildren()
            .addAll(createTableLabelUI(order), createTableServerCircularUI(order));
        vbox.setOnMouseClicked(
                event -> sidebar.loadPage("add-order", order, true));
        return vbox;
    }

    private VBox createTakeoutUI(TableOrder order) {
        VBox vbox = createVBoxUI(100, 100);

        vbox.getChildren()
            .addAll(createTableLabelUI(order), createTableServerUI(order));
        vbox.setOnMouseClicked(
                event -> sidebar.loadPage("add-order", order, false));
        return vbox;
    }

    private VBox createTableUIFixedPos( float x, float y, String name ){

        VBox vbox = createVBoxUI(100, 100);
        vbox.setLayoutX(x);
        vbox.setLayoutY(y);
        /*vbox.getChildren().addAll(createTableLabelUI(order), createTableServerUI(order));
        vbox.setOnMouseClicked(
                event -> sidebar.loadPage("add-order", order, false));
                */


        for (TableOrder table : tables) {
            if (table.getTableName().equals(name)) {
                final TableOrder finalOrder = table; // Variable final dentro del if
                vbox.setOnMouseClicked(
                        event -> sidebar.loadPage("add-order", finalOrder, true));
                vbox.getChildren().addAll(createTableLabelCircularUI(finalOrder), createTableServerCircularUI(finalOrder));
                vbox.setStyle("-fx-background-color:  #ff4d4d;");


                return vbox;
            }
        }

        TableOrder newOrder = createTableOrder();
        newOrder.setTableName(name);
        tables.add(newOrder); // Add the new order to the tables list

        vbox.getChildren().addAll(createTableLabelCircularUI(newOrder), createTableServerCircularUI(newOrder));
        vbox.setOnMouseClicked(
                event -> sidebar.loadPage("add-order", newOrder, true));
        vbox.setStyle("-fx-background-color: #66ff99;");
        return vbox;
    }


    private VBox createHTableUIFixedPos( float x, float y, String name ){

        VBox vbox = createVBoxUI(200, 100);
        vbox.setLayoutX(x);
        vbox.setLayoutY(y);
        /*vbox.getChildren().addAll(createTableLabelUI(order), createTableServerUI(order));
        vbox.setOnMouseClicked(
                event -> sidebar.loadPage("add-order", order, false));
                */


        for (TableOrder table : tables) {
            if (table.getTableName().equals(name)) {
                final TableOrder finalOrder = table; // Variable final dentro del if
                vbox.setOnMouseClicked(
                        event -> sidebar.loadPage("add-order", finalOrder, true));
                vbox.getChildren().addAll(createTableLabelCircularUI(finalOrder), createTableServerCircularUI(finalOrder));
                vbox.setStyle("-fx-background-color:  #ff4d4d;");


                return vbox;
            }
        }

        TableOrder newOrder = createTableOrder();
        newOrder.setTableName(name);
        tables.add(newOrder); // Add the new order to the tables list

        vbox.getChildren().addAll(createTableLabelCircularUI(newOrder), createTableServerCircularUI(newOrder));
        vbox.setOnMouseClicked(
                event -> sidebar.loadPage("add-order", newOrder, true));
        vbox.setStyle("-fx-background-color: #66ff99;");
        return vbox;
    }


    private VBox createVTableUIFixedPos( float x, float y, String name ){

        VBox vbox = createVBoxUI(100, 175);
        vbox.setLayoutX(x);
        vbox.setLayoutY(y);
        /*vbox.getChildren().addAll(createTableLabelUI(order), createTableServerUI(order));
        vbox.setOnMouseClicked(
                event -> sidebar.loadPage("add-order", order, false));
                */


        for (TableOrder table : tables) {
            if (table.getTableName().equals(name)) {
                final TableOrder finalOrder = table; // Variable final dentro del if
                vbox.setOnMouseClicked(
                        event -> sidebar.loadPage("add-order", finalOrder, true));
                vbox.getChildren().addAll(createTableLabelCircularUI(finalOrder), createTableServerCircularUI(finalOrder));
                vbox.setStyle("-fx-background-color:  #ff4d4d;");


                return vbox;
            }
        }

        TableOrder newOrder = createTableOrder();
        newOrder.setTableName(name);
        tables.add(newOrder); // Add the new order to the tables list

        vbox.getChildren().addAll(createTableLabelCircularUI(newOrder), createTableServerCircularUI(newOrder));
        vbox.setOnMouseClicked(
                event -> sidebar.loadPage("add-order", newOrder, true));
        vbox.setStyle("-fx-background-color: #66ff99;");
        return vbox;
    }

    private Pane createCircularTableUIFixedPos( float x, float y, String name ){

        Pane circle = createCircleUI();
        circle.setLayoutX(x);
        circle.setLayoutY(y);


        for (TableOrder table : tables) {
            if (table.getTableName().equals(name)) {
                final TableOrder finalOrder = table; // Variable final dentro del if
                circle.setOnMouseClicked(
                        event -> sidebar.loadPage("add-order", finalOrder, true));
                circle.getChildren().addAll(createTableLabelCircularUI(finalOrder), createTableServerCircularUI(finalOrder));
                circle.setStyle("-fx-background-color:  #ff4d4d;");
                return circle;
            }
        }

        TableOrder newOrder = createTableOrder();
        newOrder.setTableName(name);
        tables.add(newOrder); // Add the new order to the tables list

        circle.getChildren().addAll(createTableLabelCircularUI(newOrder), createTableServerCircularUI(newOrder));
        circle.setOnMouseClicked(
                event -> sidebar.loadPage("add-order", newOrder, true));
        circle.setStyle("-fx-background-color: #66ff99;");

        return circle;
    }

    private void initializeMainLobby(){

        VBox vbox = createTableUIFixedPos(260,470, "9");
        VBox vbox1 = createTableUIFixedPos(260,350,  "10");
        VBox vbox2 = createTableUIFixedPos(260,230,  "15");
        VBox vbox3 = createTableUIFixedPos(260,110, "20");
        VBox vbox4 = createTableUIFixedPos(440,470, "22");
        VBox vbox5 = createTableUIFixedPos(440,350,  "23");
        VBox vbox6 = createTableUIFixedPos(440,230,  "24");
        VBox vbox7 = createTableUIFixedPos(440,110, "30");
        VBox vbox8 = createTableUIFixedPos(440,0, "32");
        VBox vbox9 = createTableUIFixedPos(620,470, "34");
        VBox vbox10 = createTableUIFixedPos(620,350,  "35");
        VBox vbox11 = createTableUIFixedPos(620,230,  "38");
        VBox vbox12 = createTableUIFixedPos(620,110, "40");
        VBox vbox13 = createTableUIFixedPos(620,0, "42");
        VBox vbox14 = createTableUIFixedPos(800,470, "43");
        VBox vbox15 = createTableUIFixedPos(800,350,  "45");
        VBox vbox16 = createTableUIFixedPos(800,230,  "46");
        VBox vbox17 = createTableUIFixedPos(800,110, "47");
        VBox vbox18 = createTableUIFixedPos(800,0, "49");
        VBox vbox19 = createTableUIFixedPos(980,470, "50");
        VBox vbox20 = createTableUIFixedPos(980,350,  "52");
        VBox vbox21 = createTableUIFixedPos(980,230,  "53");
        VBox vbox22 = createTableUIFixedPos(980,110, "54");
        VBox vbox23 = createTableUIFixedPos(980,0, "55");
        Pane circulo = createCircularTableUIFixedPos(0,350, "12");
        Pane circulo1 = createCircularTableUIFixedPos(0,470, "27");
        MesasaOrg.getChildren().add(vbox);
        MesasaOrg.getChildren().add(vbox1);
        MesasaOrg.getChildren().add(vbox2);
        MesasaOrg.getChildren().add(vbox3);
        MesasaOrg.getChildren().add(vbox4);
        MesasaOrg.getChildren().add(vbox5);
        MesasaOrg.getChildren().add(vbox6);
        MesasaOrg.getChildren().add(vbox7);
        MesasaOrg.getChildren().add(vbox8);
        MesasaOrg.getChildren().add(vbox9);
        MesasaOrg.getChildren().add(vbox10);
        MesasaOrg.getChildren().add(vbox11);
        MesasaOrg.getChildren().add(vbox12);
        MesasaOrg.getChildren().add(vbox13);
        MesasaOrg.getChildren().add(vbox14);
        MesasaOrg.getChildren().add(vbox15);
        MesasaOrg.getChildren().add(vbox16);
        MesasaOrg.getChildren().add(vbox17);
        MesasaOrg.getChildren().add(vbox18);
        MesasaOrg.getChildren().add(vbox19);
        MesasaOrg.getChildren().add(vbox20);
        MesasaOrg.getChildren().add(vbox21);
        MesasaOrg.getChildren().add(vbox22);
        MesasaOrg.getChildren().add(vbox23);
        MesasaOrg.getChildren().add(circulo);
        MesasaOrg.getChildren().add(circulo1);


    }


    private void initializeTerrace(){

        VBox vbox = createHTableUIFixedPos(150,200, "1");
        VBox vbox1 = createHTableUIFixedPos(700,200,  "2");
        Terraza.getChildren().add(vbox);
        Terraza.getChildren().add(vbox1);



    }









}
