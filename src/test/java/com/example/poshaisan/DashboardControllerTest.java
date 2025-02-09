package com.example.poshaisan;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class DashboardControllerTest extends ApplicationTest {

    private static DashboardController controller;
    private static Connection connection;
    private static DB db;

    @BeforeAll
    public static void setUpClass() throws Exception {

        if (Platform.isFxApplicationThread()) {
            Platform.startup(() -> {
            });
        }

        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setPort(3307);
        db = DB.newEmbeddedDB(config.build());
        db.start();
        db.createDB("testdb");
        connection = DriverManager.getConnection("jdbc:mariadb://localhost" +
                                                 ":3307/testdb", "root",
                                                 "root");

        LocalDate today = LocalDate.now() ;
        LocalDate twoDaysAgo = today.minusDays(2);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                 CREATE TABLE IF NOT EXISTS `orders` (
                   `id` int NOT NULL AUTO_INCREMENT,
                   `isTable` tinyint NOT NULL,
                   `products` json DEFAULT NULL,
                   `server` varchar(45) DEFAULT NULL,
                   `startDateTime` datetime NOT NULL,
                   `endDateTime` datetime NOT NULL,
                   `total` int DEFAULT '0',
                   PRIMARY KEY (`id`)
                 ) ENGINE=InnoDB AUTO_INCREMENT=122 DEFAULT \
                 CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
                 """);

            stmt.execute(String.format(
                    "INSERT INTO `orders` (`isTable`, `products`, `server`, " +
                    "`startDateTime`, `endDateTime`, `total`)\n" +
                    "VALUES (1, " +
                    "'[{\"name\":\"item1\"," +
                    "\"quantity\":2}]', 'Server1'," +
                    " '%s 10:00:00', '%s 10:45:00', 1000);",
                    today, today));

            stmt.execute(String.format(
                    "INSERT INTO `orders` (`isTable`, `products`, `server`, " +
                    "`startDateTime`, `endDateTime`, `total`)\n" +
                    "VALUES (1, " +
                    "'[{\"name\":\"item2\"," +
                    "\"quantity\":3}]', 'Server1'," +
                    " '%s 11:00:00', '%s 13:00:00', 3000);",
                    today, today));

            stmt.execute(String.format(
                    "INSERT INTO `orders` (`isTable`, `products`, `server`, " +
                    "`startDateTime`, `endDateTime`, `total`)\n" +
                    "VALUES (0, " +
                    "'[{\"name\":\"item3\"," +
                    "\"quantity\":1}]', 'Server3', '%s 12:00:00', '%s " +
                    "14:00:00', 5000);",
                    today, today));

            stmt.execute(String.format(
                    "INSERT INTO `orders` (`isTable`, `products`, `server`, " +
                    "`startDateTime`, `endDateTime`, `total`)\n" +
                    "VALUES (1, '[{\"name\":\"item1\",\"quantity\":2}]', " +
                    "'Server2', '%s 11:00:00', '%s 13:00:00', 7000);",
                    twoDaysAgo, twoDaysAgo));

            stmt.execute(String.format(
                    "INSERT INTO `orders` (`isTable`, `products`, `server`, " +
                    "`startDateTime`, `endDateTime`, `total`)\n" +
                    "VALUES (0, '[{\"name\":\"item1\",\"quantity\":2}]', " +
                    "'Server3', '%s 12:00:00', '%s 14:00:00', 10000);",
                    twoDaysAgo, twoDaysAgo));
        }

        // Initiialize labels and graphics

        controller = new DashboardController(
                "jdbc:mariadb://localhost:3307/testdb", "root", "root");
        controller.dailyProfitOrder = new Label();
        controller.dailyProfitTable = new Label();
        controller.weeklyProfitOrder = new Label();
        controller.weeklyProfitTable = new Label();
        controller.dailyOrder = new Label();
        controller.weeklyOrder = new Label();
        controller.dailyTable = new Label();
        controller.weeklyTable = new Label();
        controller.meanRetentionLabel = new Label();
        controller.ordersChart = new PieChart();
        controller.serverChart = new PieChart();
        controller.tablesChartXAxis = new NumberAxis();
        controller.tablesChartYAxis = new NumberAxis();
        controller.tablesChart = new LineChart<>(controller.tablesChartXAxis,
                                                 controller.tablesChartYAxis);

        // Initialize the controller
        controller.initialize();
    }

    @AfterAll
    public static void closeConnection() throws SQLException,
            ManagedProcessException {
        if (connection != null && !connection.isClosed()) {
            try (var stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS orders");
            }
            connection.close();
        }
        if (db != null) {
            db.stop();
        }
    }

    /**
     * Auxiliary funcion that checks if a day is monday or tuesday
     */
    public boolean isBeginningOfWeek(){
        return LocalDate.now().getDayOfWeek() == DayOfWeek.TUESDAY || LocalDate.now().getDayOfWeek() == DayOfWeek.MONDAY;
    }
    @Test
    public void testDailyTableEarnings() {
            assertThat(controller.dailyProfitTable, notNullValue());
            assertThat(controller.dailyProfitTable.getText(), is("$4.000"));

    }

    @Test
    public void testDailyTakeoutEarnings() {
        assertThat(controller.dailyProfitOrder, notNullValue());
        assertThat(controller.dailyProfitOrder.getText(), is("$5.000"));
    }

    @Test
    public void testDailyTableQuantity() {
        assertThat(controller.dailyTable, notNullValue());
        assertThat(controller.dailyTable.getText(), is("2"));
    }

    @Test
    public void testDailyTakeoutQuantity() {
        assertThat(controller.dailyOrder, notNullValue());
        assertThat(controller.dailyOrder.getText(), is("1"));
    }

    @Test
    public void testWeeklyTableEarnings() {
        if (isBeginningOfWeek()) {
            assertThat(controller.weeklyProfitTable, notNullValue());
            assertThat(controller.weeklyProfitTable.getText(), is("$4.000"));
        }else {
            assertThat(controller.weeklyProfitTable, notNullValue());
            assertThat(controller.weeklyProfitTable.getText(), is("$11.000"));
        }

    }

    @Test
    public void testWeeklyTakeoutEarnings() {
        if (isBeginningOfWeek()) {
            assertThat(controller.weeklyProfitOrder, notNullValue());
            assertThat(controller.weeklyProfitOrder.getText(), is("$5.000"));
        }else {
            assertThat(controller.weeklyProfitOrder, notNullValue());
            assertThat(controller.weeklyProfitOrder.getText(), is("$15.000"));
        }
    }

    @Test
    public void testWeeklyTableQuantity() {
        if (isBeginningOfWeek()) {
            assertThat(controller.weeklyTable, notNullValue());
            assertThat(controller.weeklyTable.getText(), is("2"));
        }else {
            assertThat(controller.weeklyTable, notNullValue());
            assertThat(controller.weeklyTable.getText(), is("3"));
        }
    }

    @Test
    public void testWeeklyOrderQuantity() {
        if (isBeginningOfWeek()) {
            assertThat(controller.weeklyOrder, notNullValue());
            assertThat(controller.weeklyOrder.getText(), is("1"));
        }else {
            assertThat(controller.weeklyOrder, notNullValue());
            assertThat(controller.weeklyOrder.getText(), is("2"));
        }

    }

    @Test
    public void testTablesChart() {
        assertNotNull(controller.tablesChart,
                      "The tablesChart should not be null");

        ObservableList<XYChart.Series<Number, Number>> seriesList =
                controller.tablesChart.getData();
        assertThat(seriesList.size(), is(not(0)));

        XYChart.Series<Number, Number> series = seriesList.getFirst();
        ObservableList<XYChart.Data<Number, Number>> data = series.getData();

        XYChart.Data<Number, Number> firstDataPoint = data.getFirst();
        assertNotNull(firstDataPoint,
                      "The first data point should not be null");

        // The X value should be 600, since the start of the day is 00:00.
        assertThat(firstDataPoint.getXValue().intValue(), is(600));

        // The Y value should be 1 since there is only one table at 10:00.
        assertThat(firstDataPoint.getYValue().intValue(), is(1));

        XYChart.Data<Number, Number> secondDataPoint = data.get(1);
        assertNotNull(secondDataPoint, "The second data point should not be " +
                                       "null");

        // The value should be 660, since the start of the day is 00:00.
        assertThat(secondDataPoint.getXValue().intValue(), is(645));
        assertThat(secondDataPoint.getYValue().intValue(), is(0));

        XYChart.Data<Number, Number> thirdDataPoint = data.get(2);
        assertNotNull(thirdDataPoint, "The third data point should not be " +
                                      "null");

        // The value should be 720, since the start of the day is 00:00.
        assertThat(thirdDataPoint.getXValue().intValue(), is(660));
        assertThat(thirdDataPoint.getYValue().intValue(), is(1));

        XYChart.Data<Number, Number> fourthDataPoint = data.get(3);
        assertNotNull(fourthDataPoint, "The fourth data point should not be " +
                                       "null");

        // The value should be 780, since the start of the day is 00:00.
        assertThat(fourthDataPoint.getXValue().intValue(), is(780));
        assertThat(fourthDataPoint.getYValue().intValue(), is(0));

    }

    @Test
    public void testOrdersChart() {
        Integer item1Frequency;
        assertNotNull(controller.ordersChart,
                      "The ordersChart should not be null");

        // Verify that the Piechart have values and are sorted
        ObservableList<PieChart.Data> ordersData =
                controller.ordersChart.getData();
        assertThat("The ordersChart should have data", ordersData.size(),
                   is(not(0)));

        // Verify that the Piechart shows the correct frequency of products
        if (isBeginningOfWeek()) {
            item1Frequency = 2;
        }else {
            item1Frequency = 6;
        }
        boolean foundExpectedFirstItem = ordersData.stream()
                                                   .anyMatch(
                                                           data -> data.getName()
                                                                       .equals("item1") &&
                                                                   data.getPieValue() ==
                                                                   item1Frequency);

        assertThat(foundExpectedFirstItem, is(true));

        boolean foundExpectedSecondItem = ordersData.stream()
                                                    .anyMatch(
                                                            data -> data.getName()
                                                                        .equals("item2") &&
                                                                    data.getPieValue() ==
                                                                    3);

        assertThat(foundExpectedSecondItem, is(true));

        boolean foundExpectedThirdItem = ordersData.stream()
                                                   .anyMatch(
                                                           data -> data.getName()
                                                                       .equals("item3") &&
                                                                   data.getPieValue() ==
                                                                   1);

        assertThat(foundExpectedThirdItem, is(true));
    }

    @Test
    public void testCalculateMeanRetention() {
        /*
         * First table is 10:00 - 10:45 -> 45 [min]
         * Second table is 11:00 - 13:00 -> 120[min]
         * Third table is 11:00 - 13:00 -> 120[min] *This could not appear*
         * Mean Retention should be 285/3 = 95[min]
         */
        if (isBeginningOfWeek()) {
            assertThat(controller.meanRetentionLabel.getText(), is("01 Hora" +
                                                                           "(s) 22" +
                                                                           " minuto(s)"));
        }else {
            assertThat(controller.meanRetentionLabel.getText(), is("01 Hora(s) 35" +
                                                                           " minuto(s)"));
        }

    }

}
