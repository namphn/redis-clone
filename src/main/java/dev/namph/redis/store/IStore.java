package dev.namph.redis.store;

import dev.namph.redis.store.impl.Key;

public interface IStore {
    void set(Key key, RedisValue value);
    RedisValue get(Key key);
    int size();
}
