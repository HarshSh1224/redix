package store;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class InMemoryKeyValueStore implements KeyValueStore {
    private final ConcurrentHashMap<String, StoredValue> store = new ConcurrentHashMap<>();

    @Override
    public void set(String key, String value, Long expiresAt) {
        store.put(key, new StoredValue(value, expiresAt == null ? -1 : expiresAt));
    }

    @Override
    public String get(String key) {
        long ttl = ttl(key);
        if (ttl == -2) return null;

        return store.get(key).value();
    }

    @Override
    public boolean delete(String key) {
        return store.remove(key) != null;
    }

    @Override
    public boolean exists(String key) {
        return store.containsKey(key);
    }

    @Override
    public void expire(String key, long expiresAt) {
        store.computeIfPresent(key, (k, current) -> new StoredValue(current.value(), expiresAt));
    }

    @Override
    public long ttl(String key) {
        StoredValue storedValue = store.getOrDefault(key, new StoredValue("", -2));

        long expiresAt = storedValue.expiresAt();
        if (expiresAt == -1 || expiresAt == -2) return expiresAt;
        long remaining = expiresAt - System.nanoTime();

        if (remaining <= 0) {
            store.remove(key, storedValue);
            return -2;
        }

        else return TimeUnit.NANOSECONDS.toSeconds(remaining);
    }
}
