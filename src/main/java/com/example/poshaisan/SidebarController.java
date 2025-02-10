package com.example.poshaisan;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Logger;


/**
 * Controller class for the sidebar navigation in the application.
 */
public class SidebarController {

    // Fields --------------------------------------------------

    private static final Logger logger =
            Logger.getLogger(SidebarController.class.getName());
    private static SidebarController instance;
    private final Utils utils = new Utils();

    @FXML
    BorderPane borderPane;
    @FXML
    VBox contentVBox;
    @FXML
    Button dashboardBtn;
    @FXML
    Button inventoryBtn;
    @FXML
    Button salesBtn;
    @FXML
    Button logoutBtn;
    @FXML
    Label headerText2;


    // Methods --------------------------------------------------

    @FXML
    public void initialize(){
        this.headerText2.setText(utils.RESTAURANT_NAME);

    }
    /**
     * Retrieves the singleton instance of SidebarController.
     *
     * @return The singleton instance of SidebarController.
     */
    public static SidebarController getInstance() {
        if (instance == null) {
            instance = new SidebarController();
            instance.loadFXML();

        }
        return instance;
    }

    /**
     * Auxiliary function that works as a getter of the singleton. Contrary
     * to the getInstance method, this doesn't initialize the instance
     */
    public static SidebarController getCurrentInstance() {
        return instance;
    }

    /**
     * Loads a specific page into the center of the BorderPane.
     *
     * @param page    The name of the FXML file representing the page.
     * @param order   The TableOrder object to initialize the controller of the
     *                page.
     * @param isTable Boolean to specify if it is a table order.
     */
    public void loadPage(String page, TableOrder order, Boolean isTable) {
        System.out.println(isTable);
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(page + ".fxml"));
            Parent root = loader.load();

            if (order != null) {
                Object controller = loader.getController();
                if (controller instanceof AddOrderController) {
                    ((AddOrderController) controller).initTableOrder(order,
                                                                     isTable);
                }
                // Add more instanceof checks for other controllers as needed
            }

            contentVBox.getChildren().clear();
            contentVBox.getChildren().add(root);

            VBox.setVgrow(root, javafx.scene.layout.Priority.ALWAYS);
        } catch (IOException e) {
            logger.severe(e.toString());
        }
    }

    public void loadHistory() {
        loadPage("history", null, false);
    }

    /**
     * Loads the dashboard page.
     */
    public void loadDashboard() {
        loadPage("dashboard", null, false);
    }

    /**
     * Loads the inventory page.
     */
    public void loadInventory() {
        loadPage("inventory", null, false);
    }

    /**
     * Loads the sales page.
     */
    public void loadSales() {
        loadPage("sales", null, false);
    }

    /**
     * Logs out the user by loading the login.fxml file.
     *
     * @param event The ActionEvent triggered by the logout button.
     * @throws IOException If there is an error loading the login.fxml
     *                     file.
     */
    public void logout(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("login.fxml"));
        Parent root = loader.load();
        Button button = (Button) event.getSource();
        Scene currentScene = button.getScene();
        currentScene.setRoot(root);
        Stage stage = (Stage) currentScene.getWindow();
        stage.show();
    }

    /**
     * Loads the FXML file and initializes the controller.
     */
    private void loadFXML() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "sidebar.fxml"));
            loader.setController(this);
            Parent root = loader.load();
            this.borderPane = (BorderPane) root.lookup("#borderPane");
            this.contentVBox = (VBox) root.lookup("#contentVBox");
        } catch (IOException e) {
            logger.severe(e.toString());
        }
    }


}