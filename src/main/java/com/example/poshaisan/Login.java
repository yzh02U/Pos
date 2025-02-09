package com.example.poshaisan;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;


/**
 * Main class that extends Application to start the JavaFX application.
 */
public class Login extends Application {

    // Fields --------------------------------------------------

    private final Utils utils = new Utils();

    // Methods -------------------------------------------------

    /**
     * The main method that launches the JavaFX application.
     *
     * @param args Command-line arguments (not used in this application).
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Starts the JavaFX application by loading the FXML file and
     * initializing the primary stage.
     *
     * @param stage The primary stage of the application.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Login.class.getResource("login.fxml"));
        stage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResource("/images/Icon" +
                                                                      ".png")).toString()));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);

        stage.setTitle("Sistema POS Restaurant " + utils.RESTAURANT_NAME);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
