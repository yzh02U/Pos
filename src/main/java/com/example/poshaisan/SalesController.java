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
   // public Tab tablesTab;
    public VBox salesSection;
    public Label headerTitle;
    // public Button addTableBtn;
    public Button addOrderBtn;
    public Tab ordersTab;
    public Pane MesasaOrg;
    public Pane Terraza;
    private int current_id = 0;
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
  //  @FXML
 //   FlowPane tablesFlowPane;
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


        PrinterConnection print = new PrinterConnection();
        //List<StoredOrder> orders = print.getOrders();
        //List<StoredOrder> orders_dos = print.getOrders_command();
        //current_id = orders.size() + orders_dos.size();
        current_id = print.Get_Latest_id_from_Command();
        System.out.println("Esta es la ultima id: " + String.valueOf(current_id));


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

     //   tables.forEach(order -> tablesFlowPane.getChildren()//          .add(createTableUI(order)));



        takeouts.forEach(order -> takeoutsFlowPane.getChildren()
                .add(createTakeoutUI(order)));


        initializeMainLobby();
        initializeTerrace();
    }

    /**
     * Loads an order with an empty list of items and the current date and
     * time in the Chilean time zone.
     * Creates a TableOrder object and loads the "add-order" page in the
     * sidebar.
     */
// En SalesController.java

    // En SalesController.java

    public void loadOrder() {
        // 1. Crear el objeto
        TableOrder newOrder = createTakeoutOrder();

        // 2. Generar el NOMBRE incremental (1001, 1002...) consultando la BD
        AddOrderDAO dao = new AddOrderDAO(); // O usa la instancia que ya tengas
        String nextTakeoutName = dao.generateNextTakeoutNumber();

        newOrder.setTableName(nextTakeoutName);

        // 3. Insertar en BD para obtener el ID Interno (Solución anterior)
        PrinterConnection print = new PrinterConnection();
        Integer realId = print.AddOrderToBD(newOrder, false); // false = Para Llevar

        if (realId != -1) {
            newOrder.setId(realId);

            // OPCIONAL: Si quieres asegurar que el nombre quede guardado en BD
            // por si AddOrderToBD no usó el nombre correcto al insertar:
            print.UpdateOrderToBD(newOrder, false);

            takeouts.add(newOrder);
            sidebar.loadPage("add-order", newOrder, false);
        } else {
            System.out.println("Error crítico al crear orden en BD");
        }
    }



    public TableOrder createTakeoutOrder() {

        current_id += 1;
        System.out.println(current_id);

        /*
        return new TableOrder(getLatestTakeoutId(),
                              FXCollections.observableArrayList(),
                              utils.getDateTime());
         */
        return new TableOrder(current_id ,
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

        current_id += 1;
        System.out.println(current_id);
        return new TableOrder(current_id,
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


    private Label createTableLabelCircularUI(String name) {
        Label tableName = new Label(name);
        tableName.setLayoutX(40); // Posición X
        tableName.setLayoutY(10);  // Posición Y

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
        server.setLayoutX(45); // Posición X
        server.setLayoutY(40);  // Posición Y

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
                vbox.getChildren().addAll(createTableLabelCircularUI(name), createTableServerCircularUI(finalOrder));
                vbox.setStyle("-fx-background-color:  #ff4d4d;");


                return vbox;
            }
        }


        vbox.getChildren().addAll(createTableLabelCircularUI(name)/*, createTableServerCircularUI(newOrder)*/);
        vbox.setOnMouseClicked(
                event ->{

                    TableOrder newOrder = new TableOrder(0, FXCollections.observableArrayList(), utils.getDateTime());
                    newOrder.setTableName(name);

                    // 2. Añadimos a la lista visual
                    tables.add(newOrder);

                    // 3. INSERTAMOS EN BD Y OBTENEMOS EL ID REAL INMEDIATAMENTE
                    PrinterConnection print = new PrinterConnection();
                    Integer realId = print.AddOrderToBD(newOrder, true);

                    // 4. ¡CRUCIAL! Sobreescribimos el ID temporal con el ID real de la BD
                    if (realId != -1) {
                        // Necesitas un método 'setId' en TableOrder. Si no existe, créalo,
                        // o recrea el objeto TableOrder con el nuevo ID.
                        // Asumiendo que TableOrder es inmutable en ID (según tu código parece serlo en el constructor),
                        // Lo mejor es reemplazar el objeto en la lista o usar reflexión,
                        // pero lo más limpio es añadir un setter en TableOrder.java: public void setId(Integer id) { this.id = id; }

                        // Si no puedes añadir setters, recreamos el objeto:
                        TableOrder realOrder = new TableOrder(realId, newOrder.getItems(), newOrder.getStartDate(), newOrder.getServer(), newOrder.getTableName());

                        // Actualizamos la referencia para pasar a la siguiente pantalla
                        sidebar.loadPage("add-order", realOrder, true);
                    }
                });
        vbox.setStyle("-fx-background-color: #4d79ff;");
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
                vbox.getChildren().addAll(createTableLabelCircularUI(name), createTableServerCircularUI(finalOrder));
                vbox.setStyle("-fx-background-color:  #ff4d4d;");


                return vbox;
            }
        }


        vbox.getChildren().addAll(createTableLabelCircularUI(name)/*, createTableServerCircularUI(newOrder)*/);
        vbox.setOnMouseClicked(
                event ->{

                    TableOrder newOrder = createTableOrder();
                    newOrder.setTableName(name);
                    tables.add(newOrder); // Add the new order to the tables list
                    PrinterConnection print = new PrinterConnection();
                    print.AddOrderToBD(newOrder, true);
                    sidebar.loadPage("add-order", newOrder, true);

                } );
        vbox.setStyle("-fx-background-color: #4d79ff;");
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
                circle.getChildren().addAll(createTableLabelCircularUI(name), createTableServerCircularUI(finalOrder));
                circle.setStyle("-fx-background-color:  #ff4d4d;");
                return circle;
            }
        }


        circle.getChildren().addAll(createTableLabelCircularUI(name)/*, createTableServerCircularUI(newOrder)*/);
        circle.setOnMouseClicked(
                event ->{

                    TableOrder newOrder = createTableOrder();
                    newOrder.setTableName(name);
                    tables.add(newOrder); // Add the new order to the tables list
                    PrinterConnection print = new PrinterConnection();
                    print.AddOrderToBD(newOrder, true);
                    sidebar.loadPage("add-order", newOrder, true);

                } );
        circle.setStyle("-fx-background-color: #4d79ff;");

        return circle;
    }

    private void initializeMainLobby() {
        List<int[]> mesasRectangulares = List.of(
                new int[]{260, 470, 9}, new int[]{260, 350, 10}, new int[]{260, 230, 15}, new int[]{260, 110, 20},
                new int[]{440, 470, 22}, new int[]{440, 350, 23}, new int[]{440, 230, 24}, new int[]{440, 110, 30},
                new int[]{440, 0, 32}, new int[]{620, 470, 34}, new int[]{620, 350, 35}, new int[]{620, 230, 38},
                new int[]{620, 110, 40}, new int[]{620, 0, 42}, new int[]{800, 470, 43}, new int[]{800, 350, 45},
                new int[]{800, 230, 46}, new int[]{800, 110, 47}, new int[]{800, 0, 49}, new int[]{980, 470, 50},
                new int[]{980, 350, 52}, new int[]{980, 230, 53}, new int[]{980, 110, 54}, new int[]{980, 0, 55}
        );

        // Agregar mesas rectangulares
        for (int[] mesa : mesasRectangulares) {
            VBox vbox = createTableUIFixedPos(mesa[0], mesa[1], String.valueOf(mesa[2]));
            MesasaOrg.getChildren().add(vbox);
        }

        // Mesas circulares
        List<int[]> mesasCirculares = List.of(
                new int[]{0, 350, 12}, new int[]{0, 470, 27}
        );

        for (int[] mesa : mesasCirculares) {
            Pane circulo = createCircularTableUIFixedPos(mesa[0], mesa[1], String.valueOf(mesa[2]));
            MesasaOrg.getChildren().add(circulo);
        }
    }



    private void initializeTerrace(){

        VBox vbox = createHTableUIFixedPos(150,200, "1");
        VBox vbox1 = createHTableUIFixedPos(700,200,  "2");
        Terraza.getChildren().add(vbox);
        Terraza.getChildren().add(vbox1);



    }












}
