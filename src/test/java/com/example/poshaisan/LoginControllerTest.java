package com.example.poshaisan;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

public class LoginControllerTest extends ApplicationTest {

    private LoginController controller;


    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login" +
                                                                  ".fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    @Test
    public void testLoginEmptyFields() {
        // Try to log in without inserting any information
        clickOn(controller.loginBtn);

        // Check there's an alert visible
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(
                DialogPane.class);
        assertThat(dialogPane, is(notNullValue()));
        assertThat(dialogPane.getContentText(), is("Por favor, rellene todos " +
                                                   "los campos e intente " +
                                                   "nuevamente"));

        // Dismiss the alert
        Button confirmBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
        clickOn(confirmBtn);

    }

    @Test
    public void testLoginEmptyPassword() {
        // Try login in without password
        clickOn(controller.username).write("Test User");
        clickOn(controller.loginBtn);

        // Check there's an alert visible
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(
                DialogPane.class);
        assertThat(dialogPane, is(notNullValue()));
        assertThat(dialogPane.getContentText(), is("Por favor, rellene todos " +
                                                   "los campos e intente " +
                                                   "nuevamente"));

        // Dismiss the alert
        Button confirmBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
        clickOn(confirmBtn);
    }

    @Test
    public void testLoginEmptyUsername() {
        // Try login in without password
        clickOn(controller.password).write("Test Password");
        clickOn(controller.loginBtn);

        // Check there's an alert visible
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(
                DialogPane.class);
        assertThat(dialogPane, is(notNullValue()));
        assertThat(dialogPane.getContentText(), is("Por favor, rellene todos " +
                                                   "los campos e intente " +
                                                   "nuevamente"));

        // Dismiss the alert
        Button confirmBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
        clickOn(confirmBtn);

    }

    @Test
    public void testLoginInvalidCredentials() {
        // Try login in without password
        clickOn(controller.username).write("Test Wrong User");
        clickOn(controller.password).write("Test Wrong Password");
        clickOn(controller.loginBtn);

        // Check there's an alert visible
        DialogPane dialogPane = lookup(".dialog-pane").queryAs(
                DialogPane.class);
        assertThat(dialogPane, is(notNullValue()));
        assertThat(dialogPane.getContentText(), is("Las credenciales son " +
                                                   "erróneas"));

        // Dismiss the alert
        Button confirmBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
        clickOn(confirmBtn);


    }

    @Test
    public void testLoginSuccesful() throws RuntimeException {
        // Try login in without password
        clickOn(controller.username).write("test");
        clickOn(controller.password).write("test");
        clickOn(controller.loginBtn);
        VBox contentVBox =
                SidebarController.getInstance().contentVBox;
        assertThat(contentVBox.lookup("#salesSection"),
                   notNullValue());
        verifyThat(contentVBox.lookup("#salesSection"),
                   isVisible());
    }
}
