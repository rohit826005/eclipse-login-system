package com.elililly.auth.repository;

import com.elililly.auth.database.DatabaseConnectionManager;
import com.elililly.auth.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite-based implementation of UserRepository using prepared statements to
 * prevent SQL injection.
 */
public class DatabaseUserRepository implements UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUserRepository.class);

    private final DatabaseConnectionManager connectionManager;

    public DatabaseUserRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (id, username, email, password_hash, created_at, last_login, active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getCreatedAt() != null ? user.getCreatedAt().toString() : LocalDateTime.now().toString());
            ps.setString(6, user.getLastLogin() != null ? user.getLastLogin().toString() : null);
            ps.setInt(7, user.isActive() ? 1 : 0);
            ps.executeUpdate();
            logger.info("Saved user to database: {}", user.getUsername());
        } catch (SQLException e) {
            logger.error("Error saving user: {}", e.getMessage());
            throw new RuntimeException("Error saving user", e);
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET username=?, email=?, password_hash=?, last_login=?, active=? WHERE id=?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getLastLogin() != null ? user.getLastLogin().toString() : null);
            ps.setInt(5, user.isActive() ? 1 : 0);
            ps.setString(6, user.getId());
            ps.executeUpdate();
            logger.info("Updated user in database: {}", user.getUsername());
        } catch (SQLException e) {
            logger.error("Error updating user: {}", e.getMessage());
            throw new RuntimeException("Error updating user", e);
        }
    }

    @Override
    public Optional<User> findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by id: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by email: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error loading all users: {}", e.getMessage());
        }
        return users;
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
            logger.info("Deleted user with id: {}", id);
        } catch (SQLException e) {
            logger.error("Error deleting user: {}", e.getMessage());
            throw new RuntimeException("Error deleting user", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking username existence: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking email existence: {}", e.getMessage());
        }
        return false;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            user.setCreatedAt(LocalDateTime.parse(createdAt));
        }
        String lastLogin = rs.getString("last_login");
        if (lastLogin != null) {
            user.setLastLogin(LocalDateTime.parse(lastLogin));
        }
        user.setActive(rs.getInt("active") == 1);
        return user;
    }
}
