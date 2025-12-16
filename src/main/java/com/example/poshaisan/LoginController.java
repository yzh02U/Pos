package com.example.poshaisan;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;


import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.application.Platform;


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
    public void initialize() {
        restaurantName.setText("Restaurant " + utils.RESTAURANT_NAME);

        // --- INICIO DEL PRE-CALENTAMIENTO ---
        // Lanzamos un hilo secundario para iniciar la conexión mientras el usuario escribe.
        Thread prewarmThread = new Thread(() -> {
            try {
                // Esto fuerza a HikariCP a crear el pool y abrir las conexiones ahora,
                // en lugar de esperar a que des clic en el botón.
                Database.getConnection(utils.DB_URL, utils.DB_USER, utils.DB_PASSWORD).close();
                System.out.println("Pool de conexiones pre-calentado con éxito.");
            } catch (Exception e) {
                // Si falla aquí no importa, el error real saltará cuando el usuario intente loguearse.
                System.out.println("Intento de pre-calentamiento fallido (no crítico): " + e.getMessage());
            }
        });

        // Configuramos como daemon para que no impida cerrar la app si se sale rápido
        prewarmThread.setDaemon(true);
        prewarmThread.start();
        // --- FIN DEL PRE-CALENTAMIENTO ---
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



    // Método nuevo para abrir la configuración
    public void openDbConfig() {
        // 1. Crear el diálogo personalizado
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Acceso Restringido");
        dialog.setHeaderText("Credenciales de Administrador requeridas");

        // 2. Configurar los botones
        ButtonType loginButtonType = new ButtonType("Acceder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // 3. Crear los campos de usuario y contraseña
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Usuario");
        PasswordField password = new PasswordField();
        password.setPromptText("Contraseña");

        grid.add(new Label("Usuario:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);
        grid.add(password, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // 4. Enfocar el campo de usuario al abrir
        Platform.runLater(username::requestFocus);

        // 5. Convertir el resultado a un par Usuario/Contraseña al hacer clic en Acceder
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        // 6. Mostrar y esperar respuesta
        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            String user = usernamePassword.getKey();
            String pass = usernamePassword.getValue();

            // 7. VALIDACIÓN SEGURA
            if ("yzh".equals(user) && "Yoquese1234$".equals(pass)) {
                // ¡Credenciales correctas! Abrimos la ventana de configuración
                showConfigWindow();
            } else {
                // Credenciales incorrectas
                customAlert.generateAlert("Acceso Denegado",
                        "Credenciales de administrador incorrectas.",
                        Alert.AlertType.ERROR).showAndWait();
            }
        });
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




    private void showConfigWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("db-config.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Configuración de Base de Datos");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            customAlert.generateAlert("Error", "No se pudo abrir la ventana de configuración.", Alert.AlertType.ERROR).showAndWait();
        }
    }

}