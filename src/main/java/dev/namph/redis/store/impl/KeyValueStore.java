package dev.namph.redis.store.impl;

import dev.namph.redis.store.IStore;
import dev.namph.redis.store.RedisValue;

import java.util.HashMap;
import java.util.Map;

public class KeyValueStore implements IStore {
    private final Map<Key, RedisValue> kv = new HashMap<>();

    @Override
    public void set(Key key, RedisValue value) {
        kv.put(key, value);
    }

    @Override
    public RedisValue get(Key key) {
        return kv.get(key);
    }

    @Override
    public int size() {
        return kv.size();
    }
}
