package com.example.poshaisan;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.print.PrinterException;
import java.time.LocalDateTime;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.Collections;

public class HistoryController {

    @FXML
    private TableView<StoredOrder> historyTable;
    @FXML
    private TableColumn<StoredOrder, Integer> orderIdColumn;
    @FXML
    private TableColumn<StoredOrder, String> orderisTableColumn;
    @FXML
    private TableColumn<StoredOrder, String> orderServerColumn;
    @FXML
    private TableColumn<StoredOrder, LocalDateTime> orderDateColumn;
    @FXML
    private TableColumn<StoredOrder, Integer> orderTotalColumn;

    @FXML
    private TableView<OrderItem> orderDetailsTable;
    @FXML
    private TableColumn<OrderItem, String> productNameColumn;
    @FXML
    private TableColumn<OrderItem, Integer> productQuantityColumn;
    @FXML
    private TableColumn<OrderItem, Integer> productTotalColumn;

    @FXML
    private Button printButton; // Botón para imprimir el pedido


    private final PrinterConnection printer = new PrinterConnection();
    private final AddOrderDAO addOrderDAO = new AddOrderDAO();
    private static final SidebarController sidebar =
            SidebarController.getInstance();

    @FXML
    public void initialize() {
        // Configurar la tabla de pedidos
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDateTime"));
        orderServerColumn.setCellValueFactory(new PropertyValueFactory<>("server"));
        orderTotalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        orderisTableColumn.setCellValueFactory(cellData -> {
            boolean isTable = cellData.getValue().getIsTable();
            return new SimpleStringProperty(isTable ? "桌子" : "打包"); // "Mesa" o "Para llevar" en chino
        });

        // Configurar la tabla de productos del pedido
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        productTotalColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Obtener pedidos de la base de datos
        List<StoredOrder> orders = addOrderDAO.fetchOrdersFromDatabase();
        Collections.reverse(orders);
        historyTable.setItems(FXCollections.observableArrayList(orders));
        historyTable.refresh();

        // Escuchar la selección de la tabla de pedidos
        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldOrder, newOrder) -> {
            if (newOrder != null) {
                loadOrderDetails(newOrder.getId());  // Cargar los productos del pedido seleccionado
            }
        });

        // Configurar el botón de imprimir
        printButton.setOnAction(event -> printSelectedOrder());
    }

    /**
     * Carga los productos del pedido seleccionado en la tabla orderDetailsTable.
     * @param orderId ID del pedido seleccionado.
     */
    private void loadOrderDetails(int orderId) {
        List<OrderItem> orderItems = addOrderDAO.fetchOrderItems(orderId);
        orderDetailsTable.setItems(FXCollections.observableArrayList(orderItems));
        orderDetailsTable.refresh();
    }

    /**
     * Imprime el pedido seleccionado utilizando PrinterConnection.
     */
    private void printSelectedOrder() {
        StoredOrder selectedOrder = historyTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            System.out.println("No hay pedido seleccionado.");
            return;
        }

        try {
            boolean isTable = selectedOrder.getIsTable(); // Obtener si es mesa o para llevar

            // Crear instancia de TableOrder usando el constructor correcto
            TableOrder tableOrder = new TableOrder(
                    selectedOrder.getId(),
                    orderDetailsTable.getItems(),  // Pasar los productos
                    selectedOrder.getStartDateTime(), // Pasar la fecha
                    selectedOrder.getServer(),  // Pasar el nombre del mesero
                    selectedOrder.getTableName() // Pasar el nombre de la mesa
            );

            // Imprimir pedido con PrinterConnection
            printer.printDetail(tableOrder, isTable, false);
            System.out.println("Pedido impreso correctamente.");

        } catch (PrinterException e) {
            System.err.println("Error al imprimir el pedido: " + e.getMessage());
        }
    }



    public void showConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认");
        alert.setHeaderText("你真的想结束这一天吗？");
        alert.setContentText("选择一个选项:");

        ButtonType yesButton = new ButtonType("是");
        ButtonType noButton = new ButtonType("不想");

        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            handleYesAction();
        } else {
            handleNoAction();
        }
    }

    private void handleYesAction() {
        addOrderDAO.deleteAllOrdersFromDatabase();
        sidebar.loadPage("sales", null, false);
        System.out.println("El día ha finalizado.");
    }

    private void handleNoAction() {
        System.out.println("Se canceló la operación.");
    }

}
