package dev.namph.redis.store;

public interface TTLStore<T> {
    void setTTL(T key, long ttl);
    long getTTL(T key);
    void removeTTL(T key);
    boolean isExpired(T key);
    T getRandomKeyWithTTL();
    int size();
}
