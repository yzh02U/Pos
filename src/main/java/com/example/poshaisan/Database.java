package com.example.poshaisan;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility class for managing database connections using HikariCP.
 * Soluciona la latencia en LAN manteniendo un pool de conexiones abiertas.
 */
public class Database {

    // Instancia estática del Pool de conexiones
    private static HikariDataSource dataSource;

    /**
     * Retrieves a connection from the HikariCP Pool.
     * * @param URL      the URL of the database
     * @param USER     the database user
     * @param PASSWORD the password for the database user
     * @return a Connection object reused from the pool (0ms latency)
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection(String URL, String USER, String PASSWORD) throws SQLException {

        // Patrón Singleton: Si el pool no existe, lo creamos.
        // Si ya existe, ignoramos los parámetros y devolvemos una conexión del pool.
        if (dataSource == null) {
            synchronized (Database.class) {
                if (dataSource == null) {
                    initDataSource(URL, USER, PASSWORD);
                }
            }
        }

        // Aquí ocurre la magia: No crea una conexión nueva, toma una prestada.
        return dataSource.getConnection();
    }

    /**
     * Inicializa el Pool de conexiones con configuraciones optimizadas para MySQL y Red LAN.
     */
    private static void initDataSource(String URL, String USER, String PASSWORD) {
        HikariConfig config = new HikariConfig();

        // Credenciales básicas
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);

        // --- CONFIGURACIÓN DE RENDIMIENTO (TUNING) ---

        // Máximo de conexiones simultáneas. 10 es perfecto para un terminal POS.
        config.setMaximumPoolSize(10);

        // Tiempo mínimo que una conexión puede estar inactiva sin cerrarse (10 minutos)
        config.setMinimumIdle(2);

        // Tiempo máximo de vida de una conexión (30 minutos)
        config.setMaxLifetime(1800000);

        // Timeout: Si el pool está lleno, esperar 30s antes de dar error.
        config.setConnectionTimeout(30000);

        // --- OPTIMIZACIONES ESPECÍFICAS DE MYSQL ---
        // Esto hace que MySQL "recuerde" las queries repetidas, acelerando el POS brutalmente.
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        dataSource = new HikariDataSource(config);
    }

    /**
     * Método opcional para cerrar el pool al apagar la aplicación.
     */
    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}