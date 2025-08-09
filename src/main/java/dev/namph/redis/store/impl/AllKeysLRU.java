package dev.namph.redis.store.impl;

import dev.namph.redis.store.EvictionPolicy;
import dev.namph.redis.store.IStore;

public class AllKeysLRU implements EvictionPolicy<Key> {
    private final IStore store;
    private final int MAX_SIZE_POOL = 16;
    private final int SAMPLE_SIZE = 5;
    private final Key[] pool = new Key[MAX_SIZE_POOL];
    private int size = 0;

    public AllKeysLRU(IStore store) {
        this.store = store;
    }

    @Override
    public Key selectEvictionCandidate() {
        while (size < MAX_SIZE_POOL && size < store.size()) {
            for (int i = 0; i < SAMPLE_SIZE; i++) {
                Key key = store.getRandomKey();
                if (key == null) {
                    break;
                }
                addToPool(key);
            }
        }

        return size > 0 ? pool[size - 1] : null;
    }

    private void addToPool(Key key) {
        // find the position to insert the key
        int pos = size;
        while (pos > 0 && pool[pos - 1].getAge() > key.getAge()) {
            pool[pos] = pool[pos - 1];
        }
        pool[pos] = key;
        if (size < MAX_SIZE_POOL) {
            size++;
        }
    }
}