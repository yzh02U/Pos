package com.example.poshaisan;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

/**
 * TablesList is a singleton class that holds a list of TableOrder objects.
 * It provides a globally accessible instance to maintain a consistent list
 * of TableOrders throughout the application.
 */
public class TablesList {

    // Fields --------------------------------------------------

    private static final SidebarController sidebar =
            SidebarController.getInstance();
    private static TablesList instance;
    private final ObservableList<TableOrder> tablesList =
            FXCollections.observableArrayList();
    private final ObservableList<TableOrder> takeoutList =
            FXCollections.observableArrayList();
    // Methods --------------------------------------------------

    /**
     * Returns the singleton instance of TablesList.
     * If the instance does not exist, it creates a new one.
     *
     * @return the singleton instance of TablesList.
     */
    public static TablesList getInstance() {
        if (instance == null) {
            instance = new TablesList();
        }
        return instance;
    }

    /**
     * Checks if there is an existing order with the same table name.
     *
     * @param tableName    The name of the table to check.
     * @param currentOrder The current order to compare against.
     * @return true if a duplicate is found, false otherwise.
     */
    public boolean checkDuplicatesTables(String tableName,
                                     TableOrder currentOrder) {
        if (Objects.equals(tableName, "-")) {
            return false;
        }

        for (TableOrder table : tablesList) {
            if (Objects.equals(table.getTableName(), tableName)
                && !Objects.equals(table.getId(), currentOrder.getId())
                &&
                Objects.equals(table.getServer(), currentOrder.getServer())) {
                return true;
            }
        }
        return false;
    }

    public boolean checkDuplicatesTakeouts(String takeoutName,
                                            TableOrder currentOrder) {
        if (Objects.equals(takeoutName, "-")) {
            return false;
        }

        for (TableOrder takeout : takeoutList) {
            if (Objects.equals(takeout.getTableName(), takeoutName)
                && !Objects.equals(takeout.getId(), currentOrder.getId())
                &&
                Objects.equals(takeout.getServer(), currentOrder.getServer())) {
                return true;
            }
        }
        return false;
    }


    /**
     * Gets the ObservableList of TableOrder objects.
     * This list can be observed for changes and used to dynamically update
     * UI components.
     *
     * @return the ObservableList containing TableOrder objects.
     */
    public ObservableList<TableOrder> getTablesList() {
        return tablesList;
    }

    public ObservableList<TableOrder> getTakeoutsList() {
        return takeoutList;
    }

    /**
     * Deletes a TableOrder from the list and navigate to sales view.
     *
     * @param order The TableOrder to delete.
     */
    public void deleteTable(TableOrder order) {
        tablesList.remove(order);
        sidebar.loadPage("sales", null, false);
    }

    public void deleteTakeout(TableOrder order) {
        takeoutList.remove(order);
        sidebar.loadPage("sales", null, false);
    }

    public void goToSales(){

        sidebar.loadPage("sales", null, false);
    }

}
