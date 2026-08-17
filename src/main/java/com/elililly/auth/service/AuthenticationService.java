package com.elililly.auth.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.elililly.auth.config.Configuration;
import com.elililly.auth.model.User;
import com.elililly.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Core authentication service providing registration, login,
 * password change, and session management.
 */
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;
    private final int bcryptCost;
    private final int minPasswordLength;

    // In-memory session store: sessionToken -> userId
    private final Map<String, String> activeSessions = new java.util.concurrent.ConcurrentHashMap<>();

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
        Configuration config = Configuration.getInstance();
        this.bcryptCost = config.getInt("security.bcrypt.cost", 12);
        this.minPasswordLength = config.getInt("security.password.min.length", 8);
    }

    /**
     * Register a new user.
     *
     * @return the newly created User
     * @throws IllegalArgumentException on validation failure
     */
    public User register(String username, String email, String password) {
        validateUsername(username);
        validateEmail(email);
        validatePassword(password);

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already taken: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered: " + email);
        }

        String passwordHash = BCrypt.withDefaults().hashToString(bcryptCost, password.toCharArray());
        User user = new User(UUID.randomUUID().toString(), username, email, passwordHash);
        userRepository.save(user);
        logger.info("User registered successfully: {}", username);
        return user;
    }

    /**
     * Authenticate a user and create a session.
     *
     * @param usernameOrEmail the username or email
     * @param password        the plain-text password
     * @return a session token on success
     * @throws IllegalArgumentException on invalid credentials
     */
    public String login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new IllegalArgumentException("Username or email is required");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }

        if (userOpt.isEmpty()) {
            logger.warn("Login attempt failed - user not found: {}", usernameOrEmail);
            throw new IllegalArgumentException("Invalid credentials");
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            logger.warn("Login attempt for inactive account: {}", usernameOrEmail);
            throw new IllegalArgumentException("Account is disabled");
        }

        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPasswordHash());
        if (!result.verified) {
            logger.warn("Login failed - incorrect password for user: {}", usernameOrEmail);
            throw new IllegalArgumentException("Invalid credentials");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.update(user);

        String sessionToken = UUID.randomUUID().toString();
        activeSessions.put(sessionToken, user.getId());
        logger.info("User logged in successfully: {}", user.getUsername());
        return sessionToken;
    }

    /**
     * Invalidate a session (logout).
     */
    public void logout(String sessionToken) {
        String userId = activeSessions.remove(sessionToken);
        if (userId != null) {
            logger.info("Session invalidated for user id: {}", userId);
        }
    }

    /**
     * Retrieve the currently authenticated user for a session token.
     */
    public Optional<User> getSessionUser(String sessionToken) {
        String userId = activeSessions.get(sessionToken);
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId);
    }

    /**
     * Change a user's password after verifying the current password.
     */
    public void changePassword(String sessionToken, String currentPassword, String newPassword) {
        Optional<User> userOpt = getSessionUser(sessionToken);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid or expired session");
        }

        User user = userOpt.get();
        BCrypt.Result result = BCrypt.verifyer().verify(currentPassword.toCharArray(), user.getPasswordHash());
        if (!result.verified) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        validatePassword(newPassword);
        String newHash = BCrypt.withDefaults().hashToString(bcryptCost, newPassword.toCharArray());
        user.setPasswordHash(newHash);
        userRepository.update(user);
        logger.info("Password changed for user: {}", user.getUsername());
    }

    /**
     * Reset a user's password directly (administrative or recovery flow).
     */
    public void resetPassword(String usernameOrEmail, String newPassword) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new IllegalArgumentException("Username or email is required");
        }
        validatePassword(newPassword);

        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        String newHash = BCrypt.withDefaults().hashToString(bcryptCost, newPassword.toCharArray());
        user.setPasswordHash(newHash);
        userRepository.update(user);
        logger.info("Password reset for user: {}", user.getUsername());
    }

    // ---- Input validation ----

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }
        if (!username.matches("[A-Za-z0-9_.-]+")) {
            throw new IllegalArgumentException("Username may only contain letters, digits, underscores, dots, and hyphens");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email address format");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (password.length() < minPasswordLength) {
            throw new IllegalArgumentException("Password must be at least " + minPasswordLength + " characters long");
        }
    }
}
