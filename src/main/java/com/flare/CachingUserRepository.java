package com.flare;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CachingUserRepository implements UserRepository {
    private static final String ALL_USERS_KEY = "users";

    private final UserRepository delegate;
    private final LRUCache<List<User>> cache;

    public CachingUserRepository(UserRepository delegate, LRUCache<List<User>> cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    @Override
    public CompletableFuture<List<User>> findAll() {
        List<User> cached = cache.get(ALL_USERS_KEY);
        if (cached != null) {
            return CompletableFuture.completedFuture(new ArrayList<>(cached));
        }
        return delegate.findAll().thenApply(users -> {
            cache.set(ALL_USERS_KEY, new ArrayList<>(users));
            return users;
        });
    }

    @Override
    public CompletableFuture<Optional<User>> findByEmail(String email) {
        List<User> cached = cache.get(ALL_USERS_KEY);
        if (cached != null) {
            return CompletableFuture.completedFuture(selectByEmail(cached, email));
        }
        return delegate.findByEmail(email);
    }

    @Override
    public CompletableFuture<Boolean> add(User user) {
        return delegate.add(user).thenApply(saved -> {
            if (saved) {
                cacheUpsert(user);
            }
            return saved;
        });
    }

    @Override
    public CompletableFuture<Boolean> update(User user) {
        return delegate.update(user).thenApply(updated -> {
            if (updated) {
                cacheUpsert(user);
            }
            return updated;
        });
    }

    private synchronized void cacheUpsert(User user) {
        List<User> cached = cache.get(ALL_USERS_KEY);
        if (cached == null) {
            return;
        }
        List<User> updated = new ArrayList<>(cached);
        updated.removeIf(existing -> existing.getId().equals(user.getId()));
        updated.add(user);
        cache.set(ALL_USERS_KEY, updated);
    }

    private Optional<User> selectByEmail(List<User> users, String email) {
        return users.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }
}
