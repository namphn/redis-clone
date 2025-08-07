package dev.namph.redis.store.impl;

import dev.namph.redis.store.EvictionPolicy;
import dev.namph.redis.store.IStore;

public class AllKeysLRU implements EvictionPolicy<Key> {
    private final IStore store;
    private final int MAX_SIZE_POOL = 16;
    private final Key[] pool = new Key[MAX_SIZE_POOL];

    public AllKeysLRU(IStore store) {
        this.store = store;
    }

    @Override
    public Key selectEvictionCandidate() {
        return null; // Not implemented yet
    }
}
