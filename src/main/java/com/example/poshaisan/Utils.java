package com.example.poshaisan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class containing lists of dish types and servers.
 */
public class Utils {

    // Fields --------------------------------------------------


    private final List<String> DISH_TYPES = Arrays.asList(
            "APERITIVOS", "ARROZ", "PESCADOS Y MARISCOS", "BEBIDAS", "OTROS",
            "CHAPSUI", "COLACIONES", "ENTRADAS", "ESPECIALES", "FIDEOS",
            "FIDEOS DE ARROZ", "MENÚS", "PARRILLADAS", "POLLOS", "SOPAS",
            "VACUNO Y CERDO", "POSTRES", "VINOS", "CERVEZAS", "CAMBIOS",
            "SALSAS", "PROMOCIONES", "APERITIVOS BEBIBLES", "PISCOLAS",
            "BAJATIVOS", "WHISKYS", "CAFE Y TE"
    );

    private final List<String> SERVERS = Arrays.asList(
            "ELENA J.", "MARCIA F.", "VERONICA R.", "FELIPE", "RUBEN"
    );

    // DATOS DE CONEXIÓN (Ya no son final, se cargan dinámicamente)
    public String DB_URL;
    public String DB_USER;
    public String DB_PASSWORD;

    // Constantes del Restaurante
    public final String RESTAURANT_NAME = "CHINA HOUSE";
    public final String RESTAURANT_ADDRESS = "VEINTIUNO DE MAYO, 977, Talagante";
    public final String RESTAURANT_PHONE  = "(2) 28145093";
    public final String RESTAURANT_RUT = "76.764.184-2";

    public Utils() {
        loadDatabaseConfig();
    }
    // Methods ------------------------------------------------

    public void loadDatabaseConfig() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            prop.load(input);

            String ip = prop.getProperty("db.ip", "localhost");
            String port = prop.getProperty("db.port", "3306");
            String dbName = prop.getProperty("db.name", "pos");

            // Construimos la URL completa
            this.DB_URL = "jdbc:mysql://" + ip + ":" + port + "/" + dbName;
            this.DB_USER = prop.getProperty("db.user", "root");
            this.DB_PASSWORD = prop.getProperty("db.password", "3812");

        } catch (IOException ex) {
            // Configuración por defecto si falla la lectura
            System.out.println("No se encontró configuración, usando Localhost.");
            this.DB_URL = "jdbc:mysql://localhost:3306/pos";
            this.DB_USER = "root";
            this.DB_PASSWORD = "3812";
        }
    }


    /**
     * Retrieves the list of dish types.
     *
     * @return The list of dish types.
     */
    public List<String> getDISH_TYPES() {
        return DISH_TYPES;
    }

    /**
     * Retrieves the list of servers.
     *
     * @return The list of servers.
     */
    public List<String> getSERVERS() {
        return SERVERS;
    }

    /**
     * Gets the current date and time in the Chilean time zone.
     *
     * @return The current date and time in the Chilean time zone as a
     * LocalDateTime object.
     */
    public LocalDateTime getDateTime() {
        ZoneId localZone = ZoneId.of("America/Santiago");
        ZonedDateTime nowInChile = ZonedDateTime.now(localZone);
        return nowInChile.toLocalDateTime();
    }

    /**
     * Converts a JSON string from MySQL to a List of items.
     *
     * @param json The JSON string representing the list of items.
     * @return A List of items converted from the JSON string.
     * @throws IOException if an error occurs during JSON processing.
     */
    public List<OrderItem> jsonToItems(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<>() {
        });
    }

}
