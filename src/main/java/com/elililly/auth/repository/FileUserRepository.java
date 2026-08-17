package com.elililly.auth.repository;

import com.elililly.auth.config.Configuration;
import com.elililly.auth.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;

/**
 * File-based (JSON) implementation of UserRepository using Gson.
 */
public class FileUserRepository implements UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(FileUserRepository.class);

    private final String filePath;
    private final Gson gson;

    public FileUserRepository() {
        this.filePath = Configuration.getInstance().get("file.storage.path", "users.json");
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
        ensureFileExists();
    }

    public FileUserRepository(String filePath) {
        this.filePath = filePath;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
        ensureFileExists();
    }

    private void ensureFileExists() {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                File parent = file.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                try (Writer writer = new FileWriter(file)) {
                    writer.write("[]");
                }
                logger.info("Created user storage file: {}", filePath);
            } catch (IOException e) {
                logger.error("Could not create storage file: {}", e.getMessage());
            }
        }
    }

    private List<User> loadAll() {
        try (Reader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<List<User>>() {}.getType();
            List<User> users = gson.fromJson(reader, listType);
            return users != null ? users : new ArrayList<>();
        } catch (IOException e) {
            logger.error("Error reading user file: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveAll(List<User> users) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            logger.error("Error writing user file: {}", e.getMessage());
        }
    }

    @Override
    public void save(User user) {
        List<User> users = loadAll();
        users.add(user);
        saveAll(users);
        logger.info("Saved new user: {}", user.getUsername());
    }

    @Override
    public void update(User user) {
        List<User> users = loadAll();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                saveAll(users);
                logger.info("Updated user: {}", user.getUsername());
                return;
            }
        }
        logger.warn("User not found for update: {}", user.getId());
    }

    @Override
    public Optional<User> findById(String id) {
        return loadAll().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return loadAll().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return loadAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return Collections.unmodifiableList(loadAll());
    }

    @Override
    public void deleteById(String id) {
        List<User> users = loadAll();
        boolean removed = users.removeIf(u -> u.getId().equals(id));
        if (removed) {
            saveAll(users);
            logger.info("Deleted user with id: {}", id);
        } else {
            logger.warn("User not found for deletion: {}", id);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return loadAll().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }

    @Override
    public boolean existsByEmail(String email) {
        return loadAll().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    // Gson type adapter for LocalDateTime

    private static class LocalDateTimeAdapter
            implements com.google.gson.JsonSerializer<LocalDateTime>,
                       com.google.gson.JsonDeserializer<LocalDateTime> {

        @Override
        public com.google.gson.JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc,
                com.google.gson.JsonSerializationContext context) {
            return new com.google.gson.JsonPrimitive(src.toString());
        }

        @Override
        public LocalDateTime deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT,
                com.google.gson.JsonDeserializationContext context) {
            return LocalDateTime.parse(json.getAsString());
        }
    }
}
