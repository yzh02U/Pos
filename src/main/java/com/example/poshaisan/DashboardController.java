package com.example.poshaisan;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.awt.print.PrinterException;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Controller class for managing and updating the dashboard view.
 */
public class DashboardController {

    // Fields ---------------------------------------------------------

    public VBox dashboardSection;
    private static final Utils utils = new Utils();
    private static final Locale chileLocale = Locale.forLanguageTag("es-CL");
    private static final NumberFormat numberFormat =
            NumberFormat.getNumberInstance(
            chileLocale);
    private static AddOrderDAO ordersDAO;
    private final PrinterConnection printerConnection = new PrinterConnection();

    @FXML
    Label dailyOrder;
    @FXML
    Label dailyProfitOrder;
    @FXML
    Label dailyProfitTable;
    @FXML
    Label dailyTable;
    @FXML
    PieChart ordersChart;
    @FXML
    PieChart serverChart;
    @FXML
    LineChart<Number, Number> tablesChart;
    @FXML
    Label weeklyOrder;
    @FXML
    Label weeklyProfitOrder;
    @FXML
    Label weeklyProfitTable;
    @FXML
    Label weeklyTable;
    @FXML
    NumberAxis tablesChartXAxis;
    @FXML
    NumberAxis tablesChartYAxis;
    @FXML
    Label meanRetentionLabel;

    // Methods -----------------------------------------------------

    /**
     * Default constructor that loads database credentials from environment
     * variables.
     */
    public DashboardController() {
        ordersDAO = new AddOrderDAO();
    }

    /**
     * Auxiliary constructor that uses the Testing Database credentials
     */
    public DashboardController(String URL, String USER, String PASSWORD) {
        ordersDAO = new AddOrderDAO(URL, USER, PASSWORD);
    }

    /**
     * Gets the top 5 items based on their quantities.
     *
     * @param items HashMap of items with their quantities.
     * @return List of top 5 items.
     */
    public static List<Map.Entry<String, Integer>> getTop5Items(
            HashMap<String, Integer> items) {
        List<Map.Entry<String, Integer>> itemList = new ArrayList<>(
                items.entrySet());
        itemList.sort((entry1, entry2) -> entry2.getValue()
                                                .compareTo(entry1.getValue()));
        return itemList.subList(0, Math.min(5, itemList.size()));
    }

    /**
     * Initializes the dashboard controller by setting up the pie chart data
     * and fetching order data from the database.
     */
    public void initialize() {
        resetOrders();

        List<StoredOrder> fetchedOrders = ordersDAO.fetchOrdersFromDatabase();
        calculateEarnings(fetchedOrders);
        ObservableList<PieChart.Data> productChartData =
                calculateProductsFrequency(
                        fetchedOrders);
        ObservableList<PieChart.Data> serversChartData =
                calculateServersFrequency(
                        fetchedOrders);
        setupPieChart(productChartData, serversChartData);
        tablesChartXAxis.setTickLabelFormatter(new TimeStringConverter());
        setupTablesDistribution(fetchedOrders);
        calculateMeanRetention(fetchedOrders);
    }
    public void getDailySummary() throws PrinterException {
        List<StoredOrder> dailyOrders =
                ordersDAO.fetchDailyOrdersFromDatabase();
        // Sort the list, tables will show first
        dailyOrders.sort((item1, item2) -> Boolean.compare(item2.getIsTable(),
                                                 item1.getIsTable()));
        printerConnection.printDailySummary(dailyOrders);

    }
    /**
     * Creates a frequency map of servers from the fetched orders.
     *
     * @param fetchedOrders List of orders fetched from the database.
     * @return HashMap of servers and their frequencies.
     */
    private static HashMap<String, Integer> getStringIntegerHashMap(
            List<StoredOrder> fetchedOrders) {
        HashMap<String, Integer> serversFrequency = new HashMap<>();

        for (StoredOrder order : fetchedOrders) {
            if (!order.getServer().equals("-") &&
                !order.getServer().isEmpty() &&
                order.getIsTable()) {
                serversFrequency.put(order.getServer(),
                                     serversFrequency.getOrDefault(
                                             order.getServer(), 0) + 1);
            }
        }
        return serversFrequency;
    }

    /**
     * Resets orders that are old (past this week's monday)
     */
    private void resetOrders() {
        ordersDAO.resetOrders();

    }

    /**
     * Calculates earnings and updates labels for daily and weekly orders and
     * profits.
     *
     * @param fetchedOrders List of orders fetched from the database.
     */
    private void calculateEarnings(List<StoredOrder> fetchedOrders) {
        int takeoutDailyEarnings = 0;
        int tablesDailyEarnings = 0;
        int takeoutWeeklyEarnings = 0;
        int tablesWeeklyEarnings = 0;
        int takeoutDailyCounter = 0;
        int tablesDailyCounter = 0;
        int takeoutWeeklyCounter = 0;
        int tablesWeeklyCounter = 0;

        for (StoredOrder order : fetchedOrders) {
            if (order.getIsTable()) {
                if (order.getEndDateTime().toLocalDate()
                         .equals(utils.getDateTime().toLocalDate())) {
                    tablesDailyCounter++;
                    tablesDailyEarnings += order.getTotal();
                }

                tablesWeeklyCounter++;
                tablesWeeklyEarnings += order.getTotal();
            } else {
                if (order.getEndDateTime().toLocalDate()
                         .equals(utils.getDateTime().toLocalDate())) {
                    takeoutDailyCounter++;
                    takeoutDailyEarnings += order.getTotal();
                }

                takeoutWeeklyCounter++;
                takeoutWeeklyEarnings += order.getTotal();
            }
        }
        updateEarningsLabels(takeoutDailyEarnings, tablesDailyEarnings,
                             takeoutWeeklyEarnings, tablesWeeklyEarnings,
                             takeoutDailyCounter, takeoutWeeklyCounter,
                             tablesDailyCounter, tablesWeeklyCounter);
    }

    /**
     * Updates earnings labels.
     */
    private void updateEarningsLabels(int takeoutDailyEarnings,
                                      int tablesDailyEarnings,
                                      int takeoutWeeklyEarnings,
                                      int tablesWeeklyEarnings,
                                      int takeoutDailyCounter,
                                      int takeoutWeeklyCounter,
                                      int tablesDailyCounter,
                                      int tablesWeeklyCounter) {
        dailyProfitOrder.setText(
                "$" + numberFormat.format(takeoutDailyEarnings));
        weeklyProfitOrder.setText(
                "$" + numberFormat.format(takeoutWeeklyEarnings));
        dailyProfitTable.setText(
                "$" + numberFormat.format(tablesDailyEarnings));
        weeklyProfitTable.setText(
                "$" + numberFormat.format(tablesWeeklyEarnings));
        dailyOrder.setText(Integer.toString(takeoutDailyCounter));
        weeklyOrder.setText(Integer.toString(takeoutWeeklyCounter));
        dailyTable.setText(Integer.toString(tablesDailyCounter));
        weeklyTable.setText(Integer.toString(tablesWeeklyCounter));

    }

    /**
     * Calculates the most sold products and returns them as pie chart data.
     *
     * @param fetchedOrders List of orders fetched from the database.
     * @return ObservableList of PieChart.Data for the most sold products.
     */
    private ObservableList<PieChart.Data> calculateProductsFrequency(
            List<StoredOrder> fetchedOrders) {
        HashMap<String, Integer> productsFrequency = new HashMap<>();

        for (StoredOrder order : fetchedOrders) {
            for (OrderItem product : order.getItems()) {
                productsFrequency.put(product.getName(),
                                      productsFrequency.getOrDefault(
                                              product.getName(), 0) +
                                      product.getQuantity());
            }
        }

        List<Map.Entry<String, Integer>> mostSoldProducts = getTop5Items(
                productsFrequency);
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> product : mostSoldProducts) {
            pieChartData.add(
                    new PieChart.Data(product.getKey(), product.getValue()));
        }
        return pieChartData;
    }

    /**
     * Sets up the pie chart with the provided data and tooltips.
     *
     * @param productsChartData ObservableList of products PieChart.Data.
     * @param serverChartData   ObservableList of servers PieChart.Data.
     */
    private void setupPieChart(ObservableList<PieChart.Data> productsChartData,
                               ObservableList<PieChart.Data> serverChartData) {

        Platform.runLater(() -> {
            ordersChart.setData(productsChartData);
            setupTooltips(productsChartData);
            serverChart.setData(serverChartData);
            setupTooltips(serverChartData);
        });
    }

    /**
     * Sets up tooltips for pie chart data.
     *
     * @param chartData ObservableList of PieChart.Data.
     */
    private void setupTooltips(ObservableList<PieChart.Data> chartData) {

        for (PieChart.Data data : chartData) {
            Tooltip tooltip = new Tooltip(
                    data.getName() + ": " + data.getPieValue());

            Tooltip.install(data.getNode(), tooltip);

            data.getNode().setOnMouseEntered(
                    event -> tooltip.show(data.getNode(), event.getScreenX(),
                                          event.getScreenY() + 10));
            data.getNode().setOnMouseExited(event -> tooltip.hide());
        }
    }

    /**
     * Calculates the frequency of servers from the fetched orders and
     * returns it as pie chart data.
     *
     * @param fetchedOrders List of orders fetched from the database.
     * @return ObservableList of PieChart.Data for the servers.
     */
    private ObservableList<PieChart.Data> calculateServersFrequency(
            List<StoredOrder> fetchedOrders) {
        HashMap<String, Integer> serversFrequency = getStringIntegerHashMap(
                fetchedOrders);
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> server : serversFrequency.entrySet()) {
            pieChartData.add(
                    new PieChart.Data(server.getKey(), server.getValue()));
        }
        return pieChartData;
    }

    /**
     * Sets up the distribution of tables over time in the line chart.
     *
     * @param fetchedOrders List of orders fetched from the database.
     */
    private void setupTablesDistribution(List<StoredOrder> fetchedOrders) {
        List<TimeEvent> events = new ArrayList<>();
        int tableCount = 0;
        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        for (StoredOrder order : fetchedOrders) {
            if (order.getIsTable() && utils.getDateTime().toLocalDate()
                                           .equals(order.getStartDateTime()
                                                        .toLocalDate())) {
                events.add(new TimeEvent(order.getStartDateTime(),
                                         TimeEventType.ARRIVAL));
                events.add(new TimeEvent(order.getEndDateTime(),
                                         TimeEventType.DEPARTURE));
            }
        }

        Collections.sort(events);

        for (TimeEvent event : events) {
            if (event.type() == TimeEventType.ARRIVAL) {
                tableCount++;
            } else {
                tableCount--;
            }
            long minutesSinceMidnight =
                    event.time().getHour() * 60 + event.time().getMinute();
            series.getData()
                  .add(new XYChart.Data<>(minutesSinceMidnight, tableCount));
        }

        tablesChart.getData().add(series);
    }

    /**
     * Calculates the mean retention time of tables and updates the label.
     *
     * @param fetchedOrders List of orders fetched from the database.
     */
    private void calculateMeanRetention(List<StoredOrder> fetchedOrders) {
        List<Duration> durations = new ArrayList<>();
        List<StoredOrder> fetchedTables =
                fetchedOrders.stream().filter(StoredOrder::getIsTable).toList();

        if (fetchedTables.isEmpty()){
            meanRetentionLabel.setText("No hay ninguna orden");
        } else {
            for (StoredOrder order : fetchedTables) {
                Duration duration = Duration.between(order.getStartDateTime(),
                                                     order.getEndDateTime());

                durations.add(duration);
            }
            Duration totalDuration = durations.stream().reduce(Duration.ZERO,
                                                               Duration::plus);
            long averageMinutes = totalDuration.toMinutes() / durations.size();
            long averageHours = TimeUnit.MINUTES.toHours(averageMinutes);
            long remainingMinutes = averageMinutes % 60;

            meanRetentionLabel.setText(
                    String.format("%02d Hora(s) %02d minuto(s)", averageHours,
                                  remainingMinutes));
        }

    }

}
