package com.flare;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCacheProvider {
    public static <T> LRUCache<T> createLRUCache(CacheLimits options) {
        final int maxItemsCount = options.getMaxItemsCount();

        final Map<String, T> entries = new LinkedHashMap<String, T>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, T> eldest) {
                return size() > maxItemsCount;
            }
        };

        return new LRUCache<T>() {
            @Override
            public synchronized T get(String key) {
                return entries.get(key);
            }

            @Override
            public synchronized void set(String key, T value) {
                entries.put(key, value);
            }
        };
    }
}
