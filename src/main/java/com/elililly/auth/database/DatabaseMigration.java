package com.elililly.auth.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles database schema creation and migration.
 */
public class DatabaseMigration {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigration.class);

    private static final String CREATE_USERS_TABLE = """
            CREATE TABLE IF NOT EXISTS users (
                id           TEXT PRIMARY KEY,
                username     TEXT NOT NULL UNIQUE,
                email        TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                created_at   TEXT NOT NULL,
                last_login   TEXT,
                active       INTEGER NOT NULL DEFAULT 1
            );
            """;

    private static final String CREATE_AUDIT_TABLE = """
            CREATE TABLE IF NOT EXISTS audit_log (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                username   TEXT NOT NULL,
                event      TEXT NOT NULL,
                timestamp  TEXT NOT NULL,
                success    INTEGER NOT NULL
            );
            """;

    private final DatabaseConnectionManager connectionManager;

    public DatabaseMigration(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void migrate() {
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_USERS_TABLE);
            stmt.execute(CREATE_AUDIT_TABLE);
            logger.info("Database schema migration completed successfully");
        } catch (SQLException e) {
            logger.error("Database migration failed: {}", e.getMessage());
            throw new RuntimeException("Database migration failed", e);
        }
    }
}
