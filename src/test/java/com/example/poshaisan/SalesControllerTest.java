package com.example.poshaisan;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

public class SalesControllerTest extends ApplicationTest {

    private SalesController controller;
    private FlowPane pane;
    private VBox contentVBox;

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("sales" +
                                                                  ".fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        //pane = controller.tablesFlowPane;
        contentVBox = SidebarController.getCurrentInstance().contentVBox;
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }


    @Test
    public void initializeTest() {
        WaitForAsyncUtils.waitForFxEvents();
        ObservableList<TableOrder> tables = TablesList.getInstance()
                                                      .getTablesList();
        Platform.runLater(() -> {
            // Check that the UI shows the correct amount of tables
            assertThat(pane.getChildren().size(), is(tables.size()));
        });

    }

    @Test
    public void loadOrderTest() {
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> {
            // Add a new takeout order
            controller.loadOrder();

            // Verify that we are on the Add Order window
            assertThat(contentVBox.lookup("#addOrderSection"), notNullValue());
            verifyThat(contentVBox.lookup("#addOrderSection"), isVisible());
        });
    }

    @Test
    public void getLatestTableIdTest() {
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> {
            ObservableList<TableOrder> tables =
                    TablesList.getInstance().getTablesList();

            if (tables.isEmpty()) {
                // Check that the method returns 1 if the list is empty
                assertThat(controller.getLatestTableId(), is(1));
            } else {

                // Check that the returned id is greater than the maximum by 1
                TableOrder lastOrder = tables.getLast();
                assertThat(controller.getLatestTableId() - lastOrder.getId(),
                           is(1));
            }
        });

    }

    @Test
    public void createTableOrderTest() {
        TableOrder newOrder = controller.createTableOrder();

        // Check that newOrder is not null
        assertThat(newOrder, notNullValue());
        assertThat(newOrder.getId(), notNullValue());
        assertThat(newOrder.getItems(), notNullValue());
        assertThat(newOrder.getStartDate(), notNullValue());
    }

    @Test
    public void showAddOrderTest() {
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> {
            // Add a new takeout order
            controller.showAddOrder();

            // Verify that we are on the Add Order window
            assertThat(contentVBox.lookup("#addOrderSection"), notNullValue());
            verifyThat(contentVBox.lookup("#addOrderSection"), isVisible());
        });
    }
}
