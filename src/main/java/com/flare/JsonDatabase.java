package com.flare;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Thin gateway over the JSON file that backs the application (our "slow
 * database").
 *
 * Centralises the low-level plumbing - the file location, the
 * {@link ObjectMapper}
 * and reading/writing the document - so repositories don't each duplicate it.
 */
public class JsonDatabase {
    private static final String DEFAULT_PATH = "db.json";

    private final File file;
    private final ObjectMapper mapper;

    public JsonDatabase() {
        this(DEFAULT_PATH);
    }

    public JsonDatabase(String path) {
        this.file = new File(path);
        this.mapper = new ObjectMapper();
    }

    public boolean exists() {
        return file.exists();
    }

    public ObjectMapper mapper() {
        return mapper;
    }

    /**
     * Reads the whole document. Use this when the result needs to be written back
     * via {@link #save}.
     */
    public ObjectNode readRoot() throws IOException {
        return (ObjectNode) mapper.readTree(file);
    }

    /**
     * Convenience read-only access to a top-level collection (e.g. "users",
     * "clients").
     */
    public ArrayNode collection(String name) throws IOException {
        return (ArrayNode) readRoot().get(name);
    }

    public void save(ObjectNode root) throws IOException {
        mapper.writeValue(file, root);
    }
}
