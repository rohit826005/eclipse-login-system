package com.elililly.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads and provides application configuration from config.properties.
 */
public class Configuration {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    private static final String CONFIG_FILE = "/config.properties";

    private final Properties properties = new Properties();

    private static Configuration instance;

    private Configuration() {
        loadProperties();
    }

    public static synchronized Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream is = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                properties.load(is);
                logger.info("Configuration loaded from {}", CONFIG_FILE);
            } else {
                logger.warn("Configuration file {} not found; using defaults", CONFIG_FILE);
            }
        } catch (IOException e) {
            logger.error("Failed to load configuration: {}", e.getMessage());
        }
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for key {}: {}", key, value);
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    /** Returns the configured storage type: FILE or DATABASE */
    public StorageType getStorageType() {
        String type = get("storage.type", "FILE").toUpperCase();
        try {
            return StorageType.valueOf(type);
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown storage type '{}', defaulting to FILE", type);
            return StorageType.FILE;
        }
    }

    public enum StorageType {
        FILE, DATABASE
    }
}
