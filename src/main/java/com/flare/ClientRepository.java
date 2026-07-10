package com.flare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.JsonNode;

public class ClientRepository {
    private static final String COLLECTION = "clients";

    private final JsonDatabase database;

    public ClientRepository() {
        this(new JsonDatabase());
    }

    public ClientRepository(JsonDatabase database) {
        this.database = database;
    }

    public CompletableFuture<Client> getById(String id) {
        return CompletableFuture.supplyAsync(() -> {
            if (!database.exists()) {
                return null;
            }
            try {
                for (JsonNode node : database.collection(COLLECTION)) {
                    if (node.get("id").asText().equals(id)) {
                        return toClient(node);
                    }
                }
                return null;
            } catch (IOException e) {
                return null;
            }
        });
    }

    public CompletableFuture<List<Client>> getAll() {
        return CompletableFuture.supplyAsync(() -> {
            List<Client> clients = new ArrayList<>();
            if (!database.exists()) {
                return clients;
            }
            try {
                for (JsonNode node : database.collection(COLLECTION)) {
                    clients.add(toClient(node));
                }
                return clients;
            } catch (IOException e) {
                return new ArrayList<>();
            }
        });
    }

    private Client toClient(JsonNode node) {
        return new Client(node.get("id").asText(), node.get("name").asText());
    }
}
