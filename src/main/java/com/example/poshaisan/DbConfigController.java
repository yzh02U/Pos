package com.example.poshaisan;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class DbConfigController {

    @FXML private TextField ipField;
    @FXML private TextField portField;
    @FXML private TextField dbNameField;
    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private final CustomAlert customAlert = new CustomAlert();

    @FXML
    public void initialize() {
        // Valores por defecto sugeridos
        portField.setText("3306");
        dbNameField.setText("pos");
    }

    public void saveConfiguration() {
        Properties prop = new Properties();
        OutputStream output = null;

        try {
            output = new FileOutputStream("config.properties");

            // Guardamos lo que el usuario escribió
            prop.setProperty("db.ip", ipField.getText().trim());
            prop.setProperty("db.port", portField.getText().trim());
            prop.setProperty("db.name", dbNameField.getText().trim());
            prop.setProperty("db.user", userField.getText().trim());
            prop.setProperty("db.password", passField.getText().trim());

            // Guardar archivo
            prop.store(output, "Configuración de Base de Datos POS");

            customAlert.generateAlert("Éxito",
                    "Configuración guardada. Por favor reinicie la aplicación.",
                    Alert.AlertType.INFORMATION).showAndWait();

            closeWindow();

        } catch (IOException io) {
            io.printStackTrace();
            customAlert.generateAlert("Error",
                    "No se pudo guardar el archivo de configuración.",
                    Alert.AlertType.ERROR).showAndWait();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void closeWindow() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }
}