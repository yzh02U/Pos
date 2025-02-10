package com.example.poshaisan;

import br.com.adilson.util.PrinterMatrix;
import javafx.collections.ObservableList;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.awt.print.PrinterException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Class for handling printer connections and formatting orders for printing.
 */
public class PrinterConnection {

    // Fields --------------------------------------------------------

    private static final Logger logger = Logger.getLogger(
            PrinterConnection.class.getName());
    private static final AddOrderDAO orderDAO = new AddOrderDAO();
    private static final Utils utils = new Utils();
    Locale chileLocale = Locale.forLanguageTag("es-CL");
    NumberFormat numberFormat = NumberFormat.getNumberInstance(chileLocale);

    // Methods --------------------------------------------------------

    public void printDailySummary(List<StoredOrder> ordersList) throws PrinterException{
        PrinterMatrix printer = new PrinterMatrix();
        FileInputStream inputStream = null;
        LocalDate today = utils.getDateTime().toLocalDate();
        int voucherSize = (ordersList.size() * 2) + 28;
        int toPrintCol = 12;
        int ordersCounter = 0;
        StoredOrder lastItem = null;
        if (!ordersList.isEmpty()){
            lastItem = ordersList.getLast();
        }

        printer.setOutSize(voucherSize, 48);
        printer.printTextWrap(1, 2, 10, 48,
                              " - " + "RESTAURANT "+ utils.RESTAURANT_NAME.toUpperCase() +
                                      " -" +
                                      " ");
        printer.printTextWrap(4,5,10,48, "RUT: "+ utils.RESTAURANT_RUT );
        printer.printTextWrap(5,6, 10,48, "TEL: "+utils.RESTAURANT_PHONE);
        printer.printTextWrap(7,8,10,48,
                              "TICKET DETALLE: " + today);
        printer.printTextWrap(9,10,0,48,
                              "=============================================");

        printer.printTextWrap(10,11,0, 5, "ORDEN");
        printer.printTextWrap(10,11, 10, 14, "MESA");
        printer.printTextWrap(10,11, 20, 30, "ID MESA");
        printer.printTextWrap(10,11,35, 48, "MONTO");

        printer.printTextWrap(11,12,0, 5, "=====");
        printer.printTextWrap(11,12, 10, 14, "====");
        printer.printTextWrap(11,12, 20, 30, "=======");
        printer.printTextWrap(11,12,35, 48, "==========");


        for(StoredOrder order : ordersList){
            printer.printTextWrap(toPrintCol, toPrintCol+1, 0, 5,
                                  Integer.toString(ordersCounter) );
            printer.printTextWrap(toPrintCol, toPrintCol+1, 10, 14,
                                  order.getIsTable() ? "SI" : "NO");
            printer.printTextWrap(toPrintCol, toPrintCol+1, 20, 30,
                                  order.getTableName());
            printer.printTextWrap(toPrintCol, toPrintCol+1, 35, 48,
                                  "$" + numberFormat.format(order.getTotal()));
            if (order.equals(lastItem)){
                printer.printTextWrap(toPrintCol+1, toPrintCol+2,0,48,"==============================================");

            } else {
                printer.printTextWrap(toPrintCol+1, toPrintCol+2,0,48,"----------------------------------------------");
            }
            toPrintCol+=2;
            ordersCounter += 1;
        }
        printer.printTextWrap(toPrintCol, toPrintCol+1, 15, 30, "MESAS: ");
        printer.printTextWrap(toPrintCol, toPrintCol+1, 35, 48,
                              "$"+numberFormat.format(getTotalTables(ordersList)));
        printer.printTextWrap(toPrintCol+1, toPrintCol+2, 15, 30, "PARA " +
                "LLEVAR: ");
        printer.printTextWrap(toPrintCol+1, toPrintCol+2, 35, 48,
                              "$"+numberFormat.format(getTotalTakeout(ordersList)));

        printer.printTextWrap(toPrintCol+2, toPrintCol+3, 0,48,
                              "==============================================" );

        printer.printTextWrap(toPrintCol+3, toPrintCol+4, 15, 30, "TOTAL: ");
        printer.printTextWrap(toPrintCol+3, toPrintCol+4, 35, 48,
                              "$"+numberFormat.format(getTotalTakeout(ordersList)+getTotalTables(ordersList)));
        printer.printTextWrap(toPrintCol+4, toPrintCol+5, 0,48,
                              "==============================================" );

        printer.toFile("impresionTotal.txt");

        try {
            inputStream = new FileInputStream("impresionTotal.txt");
        } catch (FileNotFoundException e) {
            logger.severe("File not found: " + e.getMessage());
            throw new PrinterException("File not found");
        }

        DocFlavor docFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
        Doc document = new SimpleDoc(inputStream, docFormat, null);
        PrintRequestAttributeSet attributeSet =
                new HashPrintRequestAttributeSet();
        PrintService printService =
                PrintServiceLookup.lookupDefaultPrintService();

        if (printService == null) {
            logger.severe("No printer found");
            throw new PrinterException("No printer found");
        }

        DocPrintJob printJob = printService.createPrintJob();

        try {
            printJob.print(document, attributeSet);
        } catch (PrintException e) {
            logger.severe("Printing failed: " + e.getMessage());
            throw new PrinterException("Printing failed");
        }
    }

    /**
     * Prints the order to a file and sends it to the printer.
     *
     * @param order   the TableOrder object containing order details
     * @param isTable boolean indicating if the order is for a table
     * @throws PrinterException if there is an error during printing
     */
    public void printOrder(TableOrder order,
                           boolean isTable, boolean isCopy) throws PrinterException {
        PrinterMatrix printer = new PrinterMatrix();
        FileInputStream inputStream = null;
        int voucherSize = (isTable) ?
                order.getItems().size() + 35 : order.getItems().size() + 31;

        printer.setOutSize(voucherSize, 48);

        printHeader(printer);
        formatDate(printer);
        formatTime(printer);
        printOrderDetails(printer, order);
        int counter = formatOrderItems(order.getItems(), printer);
        formatSummary(order, isTable, printer, counter);

        printer.toFile("impresion.txt");

        try {
            inputStream = new FileInputStream("impresion.txt");
        } catch (FileNotFoundException e) {
            logger.severe("File not found: " + e.getMessage());
            throw new PrinterException("File not found");
        }

        DocFlavor docFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
        Doc document = new SimpleDoc(inputStream, docFormat, null);
        PrintRequestAttributeSet attributeSet =
                new HashPrintRequestAttributeSet();
        PrintService printService =
                PrintServiceLookup.lookupDefaultPrintService();

        if (printService == null) {
            logger.severe("No printer found");
            throw new PrinterException("No printer found");
        }

        DocPrintJob printJob = printService.createPrintJob();

        try {
            printJob.print(document, attributeSet);
            if (!isCopy){
                orderDAO.addOrderToDatabase(order, isTable, utils.getDateTime());
                orderDAO.deleteOrderFromDatabase_Command(order.getId());

            }
        } catch (PrintException e) {
            logger.severe("Printing failed: " + e.getMessage());
            throw new PrinterException("Printing failed");
        }
    }


    public void printDetail(TableOrder order,
                           boolean isTable, boolean isCopy) throws PrinterException {
        PrinterMatrix printer = new PrinterMatrix();
        FileInputStream inputStream = null;
        int voucherSize = (isTable) ?
                order.getItems().size() + 35 : order.getItems().size() + 31;

        printer.setOutSize(voucherSize, 48);

        printHeader(printer);
        formatDate(printer);
        formatTime(printer);
        printOrderDetails(printer, order);
        int counter = formatOrderItems(order.getItems(), printer);
        formatSummary(order, isTable, printer, counter);

        printer.toFile("impresion.txt");

        try {
            inputStream = new FileInputStream("impresion.txt");
        } catch (FileNotFoundException e) {
            logger.severe("File not found: " + e.getMessage());
            throw new PrinterException("File not found");
        }

        DocFlavor docFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
        Doc document = new SimpleDoc(inputStream, docFormat, null);
        PrintRequestAttributeSet attributeSet =
                new HashPrintRequestAttributeSet();
        PrintService printService =
                PrintServiceLookup.lookupDefaultPrintService();

        if (printService == null) {
            logger.severe("No printer found");
            throw new PrinterException("No printer found");
        }

        DocPrintJob printJob = printService.createPrintJob();

        try {
            printJob.print(document, attributeSet);

        } catch (PrintException e) {
            logger.severe("Printing failed: " + e.getMessage());
            throw new PrinterException("Printing failed");
        }
    }

    public void Add_to_BD(TableOrder order, Boolean isTable){

        orderDAO.addOrderToDatabase(order, isTable, utils.getDateTime());
        orderDAO.deleteOrderFromDatabase_Command(order.getId());
    }

    public void Delete_from_BD(TableOrder order){

        orderDAO.deleteOrderFromDatabase_Command(order.getId());
    }

    public void printCommand(TableOrder order, boolean isTable) throws PrinterException {
        PrinterMatrix printer = new PrinterMatrix();
        FileInputStream inputStream = null;

        // Ajuste del tamaño del ticket basado en la cantidad de productos
        int voucherSize = Math.max(order.getItems().size() + 10, 20); // Evita que el ticket sea demasiado corto
        printer.setOutSize(voucherSize, 48);

        // Imprimir fecha y hora
        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        printer.printTextWrap(1, 2, 1, 19, "FECHA  : " + formattedDate);

        String formattedTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        printer.printTextWrap(1, 2, 25, 48, "Hora : " + formattedTime);

        // Imprimir detalles básicos (mesa y cajero)
        printer.printTextWrap(2, 3, 1, 10, "MESA   :");
        printer.printTextWrap(2, 3, 11, 48, order.getTableName());
        printer.printTextWrap(3, 4, 1, 10, "CAJERO :");
        printer.printTextWrap(3, 4, 11, 48, order.getServer());

        // Imprimir título de productos en tamaño doble
        printer.printTextWrap(5, 6, 1, 48, "\u001D\u0021\u0011" + "PRODUCTOS PARA COCINA" + "\u001D\u0021\u0000");

        // Formatear productos con tamaño doble
        int lastPrintedLine = formatOrderItemsBigger(order.getItems(), printer);

        // Línea divisoria final antes del corte
        lastPrintedLine++;
        printer.printTextWrap(lastPrintedLine, lastPrintedLine + 1, 1, 35, "--------------------");

        // Asegurar que el corte esté después de la última línea
        lastPrintedLine += 2;
        printer.printTextWrap(lastPrintedLine, lastPrintedLine, 1, 35, "\u001B\u0069");

        // Guardar en archivo antes de imprimir
        printer.toFile("impresion.txt");

        try {
            inputStream = new FileInputStream("impresion.txt");
        } catch (FileNotFoundException e) {
            logger.severe("File not found: " + e.getMessage());
            throw new PrinterException("File not found");
        }

        DocFlavor docFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
        Doc document = new SimpleDoc(inputStream, docFormat, null);
        PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();

        if (printService == null) {
            logger.severe("No printer found");
            throw new PrinterException("No printer found");
        }

        DocPrintJob printJob = printService.createPrintJob();

        try {
            printJob.print(document, attributeSet);
        } catch (PrintException e) {
            logger.severe("Printing failed: " + e.getMessage());
            throw new PrinterException("Printing failed");
        }
    }




    public List<StoredOrder> getOrders_command() {

        return orderDAO.fetchOrdersFromDatabase_Command();
    }

    public List<StoredOrder> getOrders() {

        return orderDAO.fetchOrdersFromDatabase();
    }

    public void AddOrderToBD(TableOrder order, boolean isTable)
    {
        orderDAO.addOrderToDatabase_Command(order, isTable, utils.getDateTime());
    }

    public void UpdateOrderToBD(TableOrder order, boolean isTable)
    {
        orderDAO.UpdateOrderInDatabase(order, isTable, utils.getDateTime());
    }

    private int getTotalTakeout(List<StoredOrder> ordersList){
        int total = 0;
        for(StoredOrder order:ordersList){
            if (!order.getIsTable()){
                total += order.getTotal();
            }
        }
        return total;
    }

    private int getTotalTables(List<StoredOrder> ordersList){
        int total = 0;
        for(StoredOrder order:ordersList){
            if (order.getIsTable()){
                total += order.getTotal();
            }
        }
        return total;
    }

    /**
     * Prints the header of the receipt.
     *
     * @param printer the PrinterMatrix instance
     */
    private void printHeader(PrinterMatrix printer) {
        printer.printTextWrap(1, 2, 10, 48,
                              " - " + "RESTAURANT "+ utils.RESTAURANT_NAME.toUpperCase() +
                " -" +
                " ");
        printer.printTextWrap(3, 4, 1, 48,
                              utils.RESTAURANT_ADDRESS);
        printer.printTextWrap(4, 5, 1, 48, utils.RESTAURANT_PHONE);
    }

    /**
     * Formats and prints the current date on the receipt.
     *
     * @param printer the PrinterMatrix instance
     */
    private void formatDate(PrinterMatrix printer) {
        String formattedDate = LocalDate.now()
                                        .format(DateTimeFormatter.ofPattern(
                                                "dd/MM/yyyy"));
        printer.printTextWrap(6, 7, 1, 19, "FECHA  : " + formattedDate);
    }

    /**
     * Formats and prints the current time on the receipt.
     *
     * @param printer the PrinterMatrix instance
     */
    private void formatTime(PrinterMatrix printer) {
        String formattedTime = LocalTime.now()
                                        .format(DateTimeFormatter.ofPattern(
                                                "HH:mm"));
        printer.printTextWrap(6, 7, 25, 48, "Hora : " + formattedTime);
    }

    /**
     * Prints order details on the receipt.
     *
     * @param printer the PrinterMatrix instance
     * @param order   the TableOrder object
     */
    private void printOrderDetails(PrinterMatrix printer, TableOrder order) {
        printer.printTextWrap(7, 8, 1, 10, "MESA   :");
        printer.printTextWrap(7, 8, 11, 48, order.getTableName());
        printer.printTextWrap(8, 9, 1, 10, "CAJERO :");
        printer.printTextWrap(8, 9, 11, 48, order.getServer());
        printer.printTextWrap(10, 11, 16, 48, "DETALLE DE MESA");
        printer.printTextWrap(12, 13, 1, 9, "PRODUCTO");
        printer.printTextWrap(12, 13, 39, 48, "PRECIO");
        printer.printTextWrap(13, 14, 1, 9, "--------");
        printer.printTextWrap(13, 14, 39, 48, "------");
    }

    /**
     * Formats and prints the items in the order.
     *
     * @param items   the list of OrderItem objects
     * @param printer the PrinterMatrix instance
     * @return the updated counter position
     */
    private int formatOrderItems(ObservableList<OrderItem> items,
                                 PrinterMatrix printer) {

        int counter = 14;

        for (OrderItem orderItem : items) {
            printer.printTextWrap(counter, counter + 1, 1, 35,
                                  orderItem.getQuantity() + "x " +
                                  orderItem.getName());
            printer.printTextWrap(counter, counter + 1, 39, 48, "$" +
                                                                numberFormat.format(
                                                                        orderItem.getPrice()));
            counter += 1;
        }
        return counter;
    }


    private int formatOrderItemsBigger(ObservableList<OrderItem> items, PrinterMatrix printer) {
        String doubleSize = "\u001D\u0021\u0011"; // Texto en doble tamaño
        String normalSize = "\u001D\u0021\u0000"; // Restaurar tamaño normal
        int counter = 6;

        for (OrderItem orderItem : items) {
            if (orderItem.getName() != null && !orderItem.getName().trim().isEmpty()) {
                printer.printTextWrap(counter, counter, 1, 35,
                        doubleSize + orderItem.getQuantity() + "x " + orderItem.getName() + normalSize);
                counter++;
            }
        }

        return counter;
    }




    /**
     * Formats and prints the summary section of the order.
     *
     * @param order   the TableOrder object containing order details
     * @param isTable boolean indicating if the order is for a table
     * @param printer the PrinterMatrix instance
     * @param counter the current counter position
     */
    private void formatSummary(TableOrder order, boolean isTable,
                               PrinterMatrix printer, int counter) {
        Locale chileLocale = Locale.forLanguageTag("es-CL");
        NumberFormat numberFormat = NumberFormat.getNumberInstance(chileLocale);
        Integer total = order.getItemsSum() - order.getDiscount();
        String totalPlusTip = numberFormat.format(total + order.getTip());

        printer.printTextWrap(counter + 1, counter + 2, 27, 38, "SUBTOTAL");
        printer.printTextWrap(counter + 1, counter + 2, 39, 48,
                              "$" + numberFormat.format(order.getItemsSum()));
        printer.printTextWrap(counter + 2, counter + 3, 27, 38, "DESCUENTO");
        printer.printTextWrap(counter + 2, counter + 3, 39, 48,
                              "$" + numberFormat.format(order.getDiscount()));
        printer.printTextWrap(counter + 3, counter + 4, 10, 48,
                              "--------------------------------------");
        printer.printTextWrap(counter + 4, counter + 5, 25, 38,
                              "TOTAL A PAGAR");
        printer.printTextWrap(counter + 4, counter + 5, 39, 48,
                              "$" + numberFormat.format(total));
        printer.printTextWrap(counter + 5, counter + 6, 10, 48,
                              "--------------------------------------");

        if (isTable) {
            printer.printTextWrap(counter + 6, counter + 7, 18, 38,
                                  "10% PROPINA SUGERIDA");
            printer.printTextWrap(counter + 6, counter + 7, 39, 48,
                                  "$" + numberFormat.format(order.getTip()));
            printer.printTextWrap(counter + 7, counter + 8, 10, 48,
                                  "--------------------------------------");
            printer.printTextWrap(counter + 8, counter + 9, 2, 38,
                                  "TOTAL A PAGAR + 10% PROPINA SUGERIDA");
            printer.printTextWrap(counter + 8, counter + 9, 39, 48,
                                  "$" + totalPlusTip);
            printer.printTextWrap(counter + 10, counter + 11, 13, 48,
                                  "NO VALIDO COMO BOLETA");
        } else {
            printer.printTextWrap(counter + 6, counter + 10, 13, 48,
                                  "NO VALIDO COMO BOLETA");
        }
    }


}
