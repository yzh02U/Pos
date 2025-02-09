package com.example.poshaisan;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class CustomAlertTest extends ApplicationTest {

    private CustomAlert customAlert;

    @Override
    public void start(Stage stage) {
        customAlert = new CustomAlert();
    }

    @Test
    void testGenerateInformationAlert() {
        Platform.runLater(() -> {
            // Generate an information alert
            Alert alert = customAlert.generateAlert("Test Title",
                                                    "Test Content",
                                                    Alert.AlertType.INFORMATION);

            // Assert its values
            assertThat(alert.getAlertType(), is(Alert.AlertType.INFORMATION));
            assertThat(alert.getTitle(), is("Test Title"));
            assertThat(alert.getHeaderText(), is(nullValue()));
            assertThat(alert.getContentText(), is("Test Content"));
        });

    }

    @Test
    void testGenerateErrorAlert() {
        Platform.runLater(() -> {

            //Generate an error alert
            Alert alert = customAlert.generateAlert("Error",
                                                    "An error occurred",
                                                    Alert.AlertType.ERROR);

            // Assert its values
            assertThat(alert.getAlertType(), is(Alert.AlertType.ERROR));
            assertThat(alert.getTitle(), is("Error"));
            assertThat(alert.getHeaderText(), is(nullValue()));
            assertThat(alert.getContentText(), is("An error occurred"));
        });
    }
}
