package store;

import config.AppConfig;
import resp.RespEncoder;
import resp.RespValue;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
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

    @Override
    public void runActiveExpiration() {
        int LIMIT = 20; // Mathematically derived
        int limit = LIMIT;
        int deleted = 0;

        while (limit > 0) {
            for (var entry : store.entrySet()) {
                long entryExpiresAt = entry.getValue().expiresAt();
                if (entryExpiresAt != -1) {
                    limit--;
                    if (System.nanoTime() >= entryExpiresAt) {
                        store.remove(entry.getKey(), entry.getValue());
                        deleted++;
                    }
                }
            }
        }

        float fractionDeleted = (float) deleted / LIMIT;

        if (fractionDeleted > 0.25) {
            runActiveExpiration();
        }
    }

    @Override
    public void rewriteAOF() throws IOException {
        try (FileWriter writer = new FileWriter(AppConfig.getAOFFileName(), true)) {
            for (var entry : store.entrySet()) {
                String command = "SET %s %s".formatted(entry.getKey(), entry.getValue().value());

                RespEncoder respEncoder = new RespEncoder();

                writer.write(respEncoder.encodeResp(new RespValue.RespArray(
                    Arrays.stream(command.split("\\s+"))
                        .map(RespValue.RespString::new)
                        .map(s -> (RespValue) s)
                        .toList()
                )));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }
}
