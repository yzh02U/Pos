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
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

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

    public void printDailySummary(List<StoredOrder> ordersList) throws PrinterException {
        PrinterMatrix printer = new PrinterMatrix();
        FileInputStream inputStream = null;
        LocalDate today = utils.getDateTime().toLocalDate();

        // --- 1. CÁLCULO PRECISO DEL TAMAÑO DE PAPEL ---
        // Encabezado compacto: Título, Datos, Columnas y Separador = 6 líneas aprox.
        int headerLines = 6;

        // Cuerpo: Cada orden ocupa 1 línea de datos + 1 línea de separador.
        int bodyLines = ordersList.size() * 2;

        // Footer: Totales (Mesas, Llevar, Separador, Total Final, Separador) = 5 líneas.
        int footerLines = 5;

        // Avance para corte (Feed)
        int feedLines = 4;

        // Altura Total Exacta
        int totalHeight = headerLines + bodyLines + footerLines + feedLines + 1;

        printer.setOutSize(totalHeight, 48);

        // --- 2. IMPRESIÓN ENCABEZADO (Secuencial sin huecos) ---
        int currentLine = 0;

        // L1: Título Centrado (aprox)
        printer.printTextWrap(currentLine, currentLine + 1, 5, 48,
                "RESUMEN DIARIO - " + utils.RESTAURANT_NAME);
        currentLine++;

        // L2: RUT y Teléfono en una línea
        printer.printTextWrap(currentLine, currentLine + 1, 1, 48,
                "RUT:" + utils.RESTAURANT_RUT + " TEL:" + utils.RESTAURANT_PHONE);
        currentLine++;

        // L3: Fecha
        printer.printTextWrap(currentLine, currentLine + 1, 1, 48,
                "FECHA RESUMEN: " + today);
        currentLine++;

        // L4: Separador y Títulos de Columnas
        printer.printTextWrap(currentLine, currentLine + 1, 1, 48,
                "================================================");
        currentLine++;

        // L5: Cabeceras de Tabla
        printer.printTextWrap(currentLine, currentLine + 1, 0, 7, "GARZON");
        printer.printTextWrap(currentLine, currentLine + 1, 9, 13, "MESA"); // "SI/NO"
        printer.printTextWrap(currentLine, currentLine + 1, 15, 30, "ID MESA");
        printer.printTextWrap(currentLine, currentLine + 1, 35, 48, "MONTO");
        currentLine++;

        // L6: Separador simple
        printer.printTextWrap(currentLine, currentLine + 1, 1, 48,
                "------------------------------------------------");
        currentLine++;

        // --- 3. IMPRESIÓN DE LA LISTA DE ÓRDENES ---
        StoredOrder lastItem = (!ordersList.isEmpty()) ? ordersList.get(ordersList.size() - 1) : null;

        for (StoredOrder order : ordersList) {
            // Imprimir Datos
            printer.printTextWrap(currentLine, currentLine + 1, 0, 7,
                    order.getServer());
            printer.printTextWrap(currentLine, currentLine + 1, 9, 13,
                    order.getIsTable() ? "SI" : "NO");
            printer.printTextWrap(currentLine, currentLine + 1, 15, 30,
                    order.getTableName());
            printer.printTextWrap(currentLine, currentLine + 1, 35, 48,
                    "$" + numberFormat.format(order.getTotal()));
            currentLine++;

            // Imprimir Separador (Doble si es el último, simple si no)
            if (order.equals(lastItem)) {
                printer.printTextWrap(currentLine, currentLine + 1, 1, 48,
                        "================================================");
            } else {
                printer.printTextWrap(currentLine, currentLine + 1, 1, 48,
                        "------------------------------------------------");
            }
            currentLine++;
        }

        // --- 4. IMPRESIÓN DE TOTALES (FOOTER) ---
        // Total Mesas
        printer.printTextWrap(currentLine, currentLine + 1, 15, 30, "MESAS:");
        printer.printTextWrap(currentLine, currentLine + 1, 35, 48,
                "$" + numberFormat.format(getTotalTables(ordersList)));
        currentLine++;

        // Total Llevar
        printer.printTextWrap(currentLine, currentLine + 1, 15, 30, "PARA LLEVAR:");
        printer.printTextWrap(currentLine, currentLine + 1, 35, 48,
                "$" + numberFormat.format(getTotalTakeout(ordersList)));
        currentLine++;

        // Separador
        printer.printTextWrap(currentLine, currentLine + 1, 1, 48,
                "================================================");
        currentLine++;

        // GRAN TOTAL
        long granTotal = getTotalTakeout(ordersList) + getTotalTables(ordersList);
        printer.printTextWrap(currentLine, currentLine + 1, 15, 30, "TOTAL DIA:");
        printer.printTextWrap(currentLine, currentLine + 1, 35, 48,
                "$" + numberFormat.format(granTotal));
        currentLine++;

        // Cierre visual
        printer.printTextWrap(currentLine, currentLine + 1, 1, 48,
                "================================================");
        currentLine++;

        // --- 5. AVANCE DE PAPEL (FEED) ---
        // Esto es lo que faltaba para que no cortes sobre el texto
        for (int i = 0; i < feedLines; i++) {
            printer.printTextWrap(currentLine, currentLine + 1, 1, 48, "");
            currentLine++;
        }

        // --- 6. CORTE ---
        String PAPER_CUT = "\u001B\u0069";
        printer.printTextWrap(currentLine, currentLine + 1, 1, 48, PAPER_CUT);

        // --- PROCESO DE ENVÍO A LA IMPRESORA ---
        printer.toFile("impresionTotal.txt");

        try {
            inputStream = new FileInputStream("impresionTotal.txt");
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

    /**
     * Prints the order to a file and sends it to the printer.
     */
    public void printOrder(TableOrder order, boolean isTable, boolean isCopy) throws PrinterException {
        PrinterMatrix printer = new PrinterMatrix();
        FileInputStream inputStream = null;

        // --- 1. CÁLCULO PRECISO DEL TAMAÑO DE PAPEL ---
        // Header + Detalles fijos ocupan exactamente hasta la línea 14.
        int baseHeaderLines = 14;
        int itemLines = order.getItems().size();

        // El resumen varía: Si es mesa incluye propina (aprox 10 líneas), si no, es más corto (aprox 6).
        // Le damos un margen de seguridad de +2 líneas por si acaso.
        int summaryLines = isTable ? 12 : 8;

        int feedLines = 4; // Avance para que pase la cuchilla

        // Tamaño total = Base + Productos + Resumen + Avance + Margen error
        int totalHeight = baseHeaderLines + itemLines + summaryLines + feedLines + 1;

        printer.setOutSize(totalHeight, 48);

        // --- 2. IMPRESIÓN DEL CONTENIDO ---
        printHeader(printer);
        formatDate(printer);
        formatTime(printer);
        printOrderDetails(printer, order);

        // formatOrderItems devuelve la línea donde quedó (generalmente 14 + n_items)
        int counter = formatOrderItems(order.getItems(), printer);

        // formatSummary imprime totales y devuelve la ULTIMA línea escrita
        int lastLine = formatSummary(order, isTable, printer, counter);

        // --- 3. AVANCE DE PAPEL (FEED) ---
        // Importante: Empezamos a avanzar DESPUÉS de la última línea del resumen
        int currentLine = lastLine + 1;

        for (int i = 0; i < feedLines; i++) {
            printer.printTextWrap(currentLine, currentLine + 1, 1, 48, "");
            currentLine++;
        }

        // --- 4. CORTE ---
        String PAPER_CUT = "\u001B\u0069";
        printer.printTextWrap(currentLine, currentLine + 1, 1, 48, PAPER_CUT);

        printer.toFile("impresion.txt");

        // --- BLOQUE DE ENVÍO A IMPRESORA (Sin cambios) ---
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
            // Solo guardamos en BD si no es copia
            if (!isCopy){
                orderDAO.addOrderToDatabase(order, isTable, utils.getDateTime());
                orderDAO.deleteOrderFromDatabase_Command(order.getTableName());
            }
        } catch (PrintException e) {
            logger.severe("Printing failed: " + e.getMessage());
            throw new PrinterException("Printing failed");
        }
    }


    public void printDetail(TableOrder order, boolean isTable, boolean isCopy) throws PrinterException {
        PrinterMatrix printer = new PrinterMatrix();
        FileInputStream inputStream = null;

        // --- 1. CÁLCULO PRECISO (Misma lógica que printOrder) ---
        int baseHeaderLines = 14;
        int itemLines = order.getItems().size();

        // Aquí isTable suele ser false para 'Llevar', pero mantenemos la lógica dinámica
        int summaryLines = isTable ? 12 : 8;
        int feedLines = 4;

        int totalHeight = baseHeaderLines + itemLines + summaryLines + feedLines + 1;

        printer.setOutSize(totalHeight, 48);

        // --- 2. IMPRESIÓN ---
        printHeader(printer);
        formatDate(printer);
        formatTime(printer);
        printOrderDetails(printer, order);

        int counter = formatOrderItems(order.getItems(), printer);
        int lastLine = formatSummary(order, isTable, printer, counter);

        // --- 3. AVANCE DE PAPEL (FEED) ---
        int currentLine = lastLine + 1;
        for (int i = 0; i < feedLines; i++) {
            printer.printTextWrap(currentLine, currentLine + 1, 1, 48, "");
            currentLine++;
        }

        // --- 4. CORTE ---
        String PAPER_CUT = "\u001B\u0069";
        printer.printTextWrap(currentLine, currentLine + 1, 1, 48, PAPER_CUT);

        printer.toFile("impresion.txt");

        // --- ENVÍO A IMPRESORA ---
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

    // Se agregan mesas a la BD
    public void Add_to_BD(TableOrder order, Boolean isTable){
        orderDAO.addOrderToDatabase(order, isTable, utils.getDateTime());
        orderDAO.deleteOrderFromDatabase_Command(order.getTableName());
    }

    public List<Integer> getNonTableOrders(){
        return orderDAO.getNonTableOrdersFromDatabase();
    }

    public List<Integer> getNonTableOrders_command(){
        return orderDAO.getNonTableOrdersFromDatabase_Command();
    }

    public void Delete_from_BD(TableOrder order){
        orderDAO.deleteOrderFromDatabase_Command(order.getTableName());
    }

    public Integer Get_Latest_id_from_Command(){
        return  orderDAO.getLastOrderIdFromDatabase_Command();
    }

    public void printCommand(TableOrder order, boolean isTable) throws PrinterException {
        PrinterMatrix printer = new PrinterMatrix();
        FileInputStream inputStream = null;

        // --- CONFIGURACIÓN DE ESPACIO ---
        // Calculamos el tamaño EXACTO para evitar residuo de papel extra.
        // Encabezado (4 líneas) + Items (N líneas) + Cierre (1 línea) +
        // AVANCE FISICO (4 líneas para llegar a la cuchilla) + Corte (1 línea).
        int headerLines = 4;
        int itemsSize = order.getItems().size();
        int footerLines = 1;
        int feedLines = 4; // 4 líneas suelen ser aprox 1.5cm, suficiente para pasar el cabezal.

        int totalLines = headerLines + itemsSize + footerLines + feedLines + 1;

        printer.setOutSize(totalLines, 48);

        // COMANDOS DE CONTROL
        String RESET = "\u001B\u0040";
        String BIG_TEXT = "\u001D\u0021\u0011";
        String NORMAL_TEXT = "\u001D\u0021\u0000";

        // --- 1. ENCABEZADO (Líneas 0 a 3) ---
        int currentLine = 0;

        String tituloMesa = isTable ? "COCINA - MESA: " + order.getTableName() : "COCINA - PARA LLEVAR";

        printer.printTextWrap(currentLine, currentLine + 1, 1, 48, RESET + tituloMesa);
        currentLine++;

        printer.printTextWrap(currentLine, currentLine + 1, 1, 48, "GARZON: " + order.getServer());
        currentLine++;

        String time = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                .format(java.time.LocalDateTime.now());
        printer.printTextWrap(currentLine, currentLine + 1, 1, 48, "HORA: " + time);
        currentLine++;

        printer.printTextWrap(currentLine, currentLine + 1, 1, 48, "--------------------------------");
        currentLine++;

        // --- 2. IMPRIMIR PRODUCTOS ---
        for (OrderItem item : order.getItems()) {
            String line = item.getQuantity() + " x " + item.getName();
            // Usamos currentLine dinámico para no perder el índice
            printer.printTextWrap(currentLine, currentLine + 1, 1, 48, BIG_TEXT + line);
            currentLine++;
        }

        // --- 3. CIERRE VISUAL ---
        printer.printTextWrap(currentLine, currentLine + 1, 1, 48, NORMAL_TEXT + "--------------------------------");
        currentLine++;

        // --- 4. AVANCE DE PAPEL (FEED) ---
        // Imprimimos líneas vacías para empujar el texto fuera del cabezal hacia la cuchilla.
        // Si tu impresora sigue cortando texto, aumenta feedLines a 5 o 6.
        for (int i = 0; i < feedLines; i++) {
            printer.printTextWrap(currentLine, currentLine + 1, 1, 48, "");
            currentLine++;
        }

        // --- 5. CORTE DE PAPEL ---
        // El corte ocurre al final exacto del avance.
        String PAPER_CUT = "\u001B\u0069";
        printer.printTextWrap(currentLine, currentLine + 1, 1, 48, PAPER_CUT);

        // --- PROCESO DE IMPRESIÓN (Igual que antes) ---
        printer.toFile("impresion_cocina.txt");

        try {
            inputStream = new FileInputStream("impresion_cocina.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();

        if (printService != null) {
            DocFlavor docFlavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            DocPrintJob docPrintJob = printService.createPrintJob();
            Doc doc = new SimpleDoc(inputStream, docFlavor, null);

            try {
                docPrintJob.print(doc, null);
            } catch (PrintException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error: No se encontró una impresora predeterminada.");
        }
    }


    public List<StoredOrder> getOrders_command() {
        return orderDAO.fetchOrdersFromDatabase_Command();
    }

    public List<StoredOrder> getOrders() {
        return orderDAO.fetchOrdersFromDatabase();
    }

    public Integer AddOrderToBD(TableOrder order, boolean isTable) {
        Integer newId = orderDAO.addOrderToDatabase_Command(order, isTable, utils.getDateTime());
        return newId;
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

    private void printHeader(PrinterMatrix printer) {
        String INIT_AND_NORMAL = "\u001B\u0040\u001D\u0021\u0000";

        printer.printTextWrap(1, 2, 10, 48,
                INIT_AND_NORMAL + " - " + "RESTAURANT "+ utils.RESTAURANT_NAME.toUpperCase() +
                        " -" +
                        " ");

        printer.printTextWrap(3, 4, 1, 48,
                utils.RESTAURANT_ADDRESS);
        printer.printTextWrap(4, 5, 1, 48, utils.RESTAURANT_PHONE);
    }

    private void formatDate(PrinterMatrix printer) {
        String formattedDate = LocalDate.now()
                .format(DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy"));
        printer.printTextWrap(6, 7, 1, 19, "FECHA  : " + formattedDate);
    }

    private void formatTime(PrinterMatrix printer) {
        String formattedTime = LocalTime.now()
                .format(DateTimeFormatter.ofPattern(
                        "HH:mm"));
        printer.printTextWrap(6, 7, 25, 48, "Hora : " + formattedTime);
    }

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

    /**
     * Formats and prints the summary section of the order.
     * MODIFICADO: Se eliminó el descuento del 5% y sus mensajes.
     *
     * @return El número de la última línea impresa.
     */
    private int formatSummary(TableOrder order, boolean isTable,
                              PrinterMatrix printer, int counter) {
        Locale chileLocale = Locale.forLanguageTag("es-CL");
        NumberFormat numberFormat = NumberFormat.getNumberInstance(chileLocale);

        // 1. Cálculos base
        Integer subTotal = order.getItemsSum();
        Integer total = subTotal - order.getDiscount(); // Total con descuento manual
        String totalPlusTip = numberFormat.format(total + order.getTip());

        // 2. Imprimir Bloque Estándar (Subtotal, Descuento Manual, Total)
        printer.printTextWrap(counter + 1, counter + 2, 27, 38, "SUBTOTAL");
        printer.printTextWrap(counter + 1, counter + 2, 39, 48,
                "$" + numberFormat.format(subTotal));

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

        int currentLine = counter + 6;

        // 3. Imprimir Bloque Propina (Solo Mesas)
        if (isTable) {
            printer.printTextWrap(currentLine, currentLine + 1, 18, 38,
                    "10% PROPINA SUGERIDA");
            printer.printTextWrap(currentLine, currentLine + 1, 39, 48,
                    "$" + numberFormat.format(order.getTip()));
            currentLine++;

            printer.printTextWrap(currentLine, currentLine + 1, 10, 48,
                    "--------------------------------------");
            currentLine++;

            printer.printTextWrap(currentLine, currentLine + 1, 2, 38,
                    "TOTAL A PAGAR + 10% PROPINA SUGERIDA");
            printer.printTextWrap(currentLine, currentLine + 1, 39, 48,
                    "$" + totalPlusTip);
            currentLine += 2;
        }

        // --- ELIMINADO: LÓGICA DE DESCUENTO 5% EFECTIVO ---
        // Se borró todo el bloque if (subTotal > 20000 && !hasRestrictedItems(order))
        // ---------------------------------------------

        // 5. Mensaje Final
        printer.printTextWrap(currentLine, currentLine + 1, 13, 48,
                "NO VALIDO COMO BOLETA");

        // Retornamos la última línea impresa para saber dónde cortar
        return currentLine;
    }




}




