package store;

public interface KeyValueStore {
    void set(String key, String value, Long expiresAt);
    String get(String key);
    boolean delete(String key);
    boolean exists(String key);
    boolean expire(String key, Long expiresAt);
    long ttl(String key);
}
