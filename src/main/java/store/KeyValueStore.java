package store;

public interface KeyValueStore {
    void set(String key, String value);
    String get(String key);
    boolean delete(String key);
    boolean exists(String key);
    void expire(String key, long expiresAt);
    long ttl(String key);
}
