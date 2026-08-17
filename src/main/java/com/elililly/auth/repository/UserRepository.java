package com.elililly.auth.repository;

import com.elililly.auth.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Abstract repository interface for User persistence.
 */
public interface UserRepository {

    /** Persist a new user. */
    void save(User user);

    /** Update an existing user. */
    void update(User user);

    /** Find user by their unique ID. */
    Optional<User> findById(String id);

    /** Find user by username (case-insensitive). */
    Optional<User> findByUsername(String username);

    /** Find user by email address (case-insensitive). */
    Optional<User> findByEmail(String email);

    /** Return all users. */
    List<User> findAll();

    /** Delete user by ID. */
    void deleteById(String id);

    /** Check whether a username is already taken. */
    boolean existsByUsername(String username);

    /** Check whether an email is already registered. */
    boolean existsByEmail(String email);
}
