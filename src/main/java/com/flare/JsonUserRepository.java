package com.flare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * {@link UserRepository} backed by the JSON {@link JsonDatabase}.
 */
public class JsonUserRepository implements UserRepository {
    private static final String COLLECTION = "users";

    private final JsonDatabase database;

    public JsonUserRepository() {
        this(new JsonDatabase());
    }

    public JsonUserRepository(JsonDatabase database) {
        this.database = database;
    }

    @Override
    public CompletableFuture<List<User>> findAll() {
        return CompletableFuture.supplyAsync(() -> {
            List<User> users = new ArrayList<>();
            if (!database.exists()) {
                return users;
            }
            try {
                for (JsonNode node : database.collection(COLLECTION)) {
                    users.add(database.mapper().treeToValue(node, User.class));
                }
                return users;
            } catch (IOException e) {
                return new ArrayList<>();
            }
        });
    }

    @Override
    public CompletableFuture<Optional<User>> findByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> {
            if (!database.exists()) {
                return Optional.empty();
            }
            try {
                for (JsonNode node : database.collection(COLLECTION)) {
                    if (node.get("email").asText().equals(email)) {
                        return Optional.of(database.mapper().treeToValue(node, User.class));
                    }
                }
                return Optional.empty();
            } catch (IOException e) {
                return Optional.empty();
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> add(User user) {
        return CompletableFuture.supplyAsync(() -> {
            if (user == null || !database.exists()) {
                return false;
            }
            try {
                ObjectNode root = database.readRoot();
                ArrayNode users = (ArrayNode) root.get(COLLECTION);
                users.add(database.mapper().valueToTree(user));
                database.save(root);
                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> update(User user) {
        return CompletableFuture.supplyAsync(() -> {
            if (user == null || !database.exists()) {
                return false;
            }
            try {
                ObjectNode root = database.readRoot();
                ArrayNode users = (ArrayNode) root.get(COLLECTION);
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).get("id").asText().equals(user.getId())) {
                        users.set(i, database.mapper().valueToTree(user));
                        database.save(root);
                        return true;
                    }
                }
                return false;
            } catch (IOException e) {
                return false;
            }
        });
    }
}
