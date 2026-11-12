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
        return isExpiredOrAbsent(key) ? null : store.get(key).value();
    }

    @Override
    public boolean delete(String key) {
        return !isExpiredOrAbsent(key) && store.remove(key) != null;
    }

    @Override
    public boolean exists(String key) {
        return !isExpiredOrAbsent(key);
    }

    @Override
    public boolean expire(String key, Long expiresAt) {
        if (!isExpiredOrAbsent(key) && expiresAt != null) {
            store.computeIfPresent(key, (k, current) -> new StoredValue(current.value(), expiresAt));
            return true;
        }
        return false;

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
        } else return TimeUnit.NANOSECONDS.toSeconds(remaining);
    }

    private boolean isExpiredOrAbsent(String key) {
        return ttl(key) == -2;
    }
}
