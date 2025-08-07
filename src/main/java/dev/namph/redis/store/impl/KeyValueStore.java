package dev.namph.redis.store.impl;

import dev.namph.redis.store.IStore;
import dev.namph.redis.store.RedisValue;
import dev.namph.redis.store.TTLStore;

import java.util.HashMap;
import java.util.Map;

public class KeyValueStore implements IStore {
    private final Map<Key, RedisValue> kv = new HashMap<>();
    private final TTLStore<Key> ttlStore;

    public KeyValueStore(TTLStore<Key> ttlStore) {
        this.ttlStore = ttlStore;
    }

    @Override
    public void set(Key key, RedisValue value) {
        kv.put(key, value);
        key.setLastRecentUse();
    }

    @Override
    public RedisValue get(Key key) {
        if (ttlStore.isExpired(key)) {
            kv.remove(key);
            ttlStore.removeTTL(key);
            return null;
        }
        key.setLastRecentUse();
        return kv.get(key);
    }

    @Override
    public int size() {
        return kv.size();
    }

    public void remove(Key key) {
        kv.remove(key);
    }
}
