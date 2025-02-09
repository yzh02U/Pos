package com.example.poshaisan;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

public class SidebarControllerTest extends ApplicationTest {

    private SidebarController controller;

    @BeforeAll
    public static void initToolkit() {
        if (Platform.isFxApplicationThread()) {
            Platform.startup(() -> {
            });
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("sidebar.fxml"));

        controller = SidebarController.getInstance();
        loader.setController(controller);

        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();


    }

    // Because TablesList is a Singleton we have to make sure we reset its state
    @BeforeEach
    public void resetSingleton() {
        try {
            java.lang.reflect.Field instance =
                    SidebarController.class.getDeclaredField(
                    "instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testLoadDashboard() {
        clickOn(controller.dashboardBtn);

        // Check that the view changes on button action
        verifyThat(controller.contentVBox.lookup("#dashboardSection"),
                   isVisible());
    }

    @Test
    public void testLoadInventory() {
        clickOn(controller.inventoryBtn);

        // Check that the view changes on button action
        verifyThat(controller.contentVBox.lookup("#inventorySection"),
                   isVisible());
    }

    @Test
    public void testLoadSales() {
        clickOn(controller.salesBtn);

        // Check that the view changes on button action
        verifyThat(controller.contentVBox.lookup("#salesSection"),
                   isVisible());
    }

    @Test
    public void testLogout() {
        clickOn(controller.logoutBtn);

        verifyThat(lookup("#loginSection"), isVisible());


    }

}
