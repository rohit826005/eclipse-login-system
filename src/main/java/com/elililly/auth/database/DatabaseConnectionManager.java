package com.elililly.auth.database;

import com.elililly.auth.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages SQLite database connections.
 */
public class DatabaseConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionManager.class);

    private final String jdbcUrl;

    private static DatabaseConnectionManager instance;

    private DatabaseConnectionManager() {
        String dbPath = Configuration.getInstance().get("db.path", "eclipse_login.db");
        this.jdbcUrl = "jdbc:sqlite:" + dbPath;
        logger.info("Database URL configured: {}", jdbcUrl);
    }

    public static synchronized DatabaseConnectionManager getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }
}
