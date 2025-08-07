package dev.namph.redis.store;

public interface EvictionPolicy<T> {
    T selectEvictionCandidate();
}
