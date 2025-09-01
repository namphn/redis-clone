package dev.namph.redis.store;

import dev.namph.redis.store.impl.Key;

public interface IStore {
    void set(byte[] key, RedisValue value);
    RedisValue get(byte[] key);
    int size();
    void remove(byte[] key);
    Key getRandomKey();
    boolean contains(byte[] key);
    Object[] getAll();
}
