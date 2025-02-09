package com.example.poshaisan;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;


/**
 * Controller class for handling login functionality.
 */
public class LoginController {

    // Fields --------------------------------------------------

    public BorderPane loginSection;
    private final LoginDAO loginDAO = new LoginDAO();
    private final CustomAlert customAlert = new CustomAlert();
    private final Utils utils = new Utils();

    @FXML
    PasswordField password;
    @FXML
    TextField username;
    @FXML
    Button loginBtn;
    @FXML
    Label restaurantName;

    //  Methods ------------------------------------------

    @FXML
    public void initialize(){
        restaurantName.setText("Restaurant " + utils.RESTAURANT_NAME);
    }
    /**
     * Handles the login action.
     * If credentials are empty, shows an error alert.
     * If login is successful, redirects to the sidebar using Singleton
     * instance.
     * If login fails, shows an error alert.
     *
     * @throws IOException  if there is an error loading sidebar.fxml.
     */
    public String login() throws IOException {
        if (credentialsAreEmpty()) {
            customAlert.generateAlert("Error",
                                      "Por favor, rellene todos los campos e " +
                                      "intente nuevamente",
                                      Alert.AlertType.ERROR)
                       .showAndWait();
            return "Por favor, rellene todos los campos e intente nuevamente";
        }

        if (loginDAO.userExists(username.getText(), password.getText())) {
            handleButtonAction();
            return null;
        } else {
            customAlert.generateAlert("Error",
                                      "Las credenciales son erróneas",
                                      Alert.AlertType.ERROR)
                       .showAndWait();
            return "Las credenciales son erróneas";
        }
    }

    /**
     * Checks if username or password is empty.
     *
     * @return true if either username or password is empty, false otherwise.
     */
    private boolean credentialsAreEmpty() {
        return username.getText().isEmpty() || password.getText().isEmpty();
    }

    /**
     * Redirects to Sidebar FXML using a Singleton instance of
     * SidebarController.
     *
     * @throws IOException if there is an error loading sidebar.fxml.
     */
    private void handleButtonAction() throws IOException {
        SidebarController sidebarController = SidebarController.getInstance();
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("sidebar.fxml"));
        loader.setController(
                sidebarController);
        Parent root = loader.load();

        Scene currentScene = username.getScene();
        currentScene.setRoot(root);
        Stage stage = (Stage) currentScene.getWindow();
        stage.show();
        sidebarController.loadPage("sales", null, false);

    }

}