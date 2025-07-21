package dev.namph.redis.store.impl;

import dev.namph.redis.store.IStore;
import java.util.HashMap;
import java.util.Map;

public class KeyValueStore implements IStore {
    private final Map<Key, byte[]> kv = new HashMap<>();

    @Override
    public synchronized void set(byte[] key, byte[] value) {
        kv.put(new Key(key), value);
    }

    @Override
    public byte[] get(byte[] key) {
        return kv.get(new Key(key));
    }

    @Override
    public int size() {
        return kv.size();
    }
}
