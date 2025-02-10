package com.example.poshaisan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.*;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class containing lists of dish types and servers.
 */
public class Utils {

    // Fields --------------------------------------------------


    private final List<String> DISH_TYPES = Arrays.asList(
            "APERITIVOS",
            "ARROZ",
            "PESCADOS Y MARISCOS",
            "BEBIDAS",
            "OTROS",
            "CHAPSUI",
            "COLACIONES",
            "ENTRADAS",
            "ESPECIALES",
            "FIDEOS",
            "FIDEOS DE ARROZ",
            "MENÚS",
            "PARRILLADAS",
            "POLLOS",
            "SOPAS",
            "VACUNO Y CERDO",
            "POSTRES",
            "VINOS",
            "CERVEZAS",
            "CAMBIOS",
            "SALSAS",
            "PROMOCIONES",
            "APERITIVOS BEBIBLES",
            "PISCOLAS",
            "BAJATIVOS",
            "WHISKYS",
            "CAFE Y TE",
            "VINOS"
    );

    private final List<String> SERVERS = Arrays.asList(
            "ELENA J.",
            "MARCIA F.",
            "VERONICA R.",
            "FELIPE",
            "RUBEN"
    );

    public final String DB_URL = "jdbc:mysql://localhost:3306/pos";
    public final String DB_USER = "root";
    public final String DB_PASSWORD = "3812";

    public final String RESTAURANT_NAME = "CHINA HOUSE";
    public final String RESTAURANT_ADDRESS = "VEINTIUNO DE MAYO, 977, Talagante";
    public final String RESTAURANT_PHONE  = "(2) 28145093";
    public final String RESTAURANT_RUT = "76.764.184-2";

    // Methods ------------------------------------------------

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
