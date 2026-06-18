package com.secureauth.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized JDBC connection manager.
 *
 * Reads DB connection parameters from AppConfig so credentials
 * are not hardcoded throughout the codebase.
 *
 * NOTE: Returns a fresh connection on every call. Callers must use
 * try-with-resources to ensure connections are closed. This is safe
 * for a single-threaded console app; a connection pool would be the
 * production-grade equivalent.
 */
public class DBConnection {

    private static final String URL =
            "jdbc:mysql://" + AppConfig.DB_HOST + ":" + AppConfig.DB_PORT +
            "/" + AppConfig.DB_NAME + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private DBConnection() {
    }

    /**
     * Returns a fresh JDBC connection. Callers should close the
     * connection via try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found on classpath.", e);
        }
        return DriverManager.getConnection(URL, AppConfig.DB_USER, AppConfig.DB_PASSWORD);
    }

    public static void close() {
        // no-op: each caller manages its own connection via try-with-resources
    }
}
