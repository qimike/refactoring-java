package com.flare;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Persistence boundary for {@link User} entities.
 *
 * Keeping this as an interface lets the storage strategy change (for example,
 * wrapping it in a caching layer) without touching {@link UserService}.
 */
public interface UserRepository {
    CompletableFuture<List<User>> findAll();

    CompletableFuture<Optional<User>> findByEmail(String email);

    CompletableFuture<Boolean> add(User user);

    CompletableFuture<Boolean> update(User user);
}
