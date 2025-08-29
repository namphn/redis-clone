package dev.namph.redis.store.impl;

import dev.namph.redis.store.TTLStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleTTLStore<T> implements TTLStore<T> {
    private final Map<T, Entry> ttlMap;
    private final List<T> ttlKeys;

    public static class Entry {
        private long expireAt;
        private int index;

        public Entry(long ttl, int index) {
            this.expireAt = ttl;
            this.index = index;
        }

        public long getExpireAt() {
            return expireAt;
        }

        public int getIndex() {
            return index;
        }

        private void setExpireAt(long expireAt) {
            this.expireAt = expireAt;
        }
    }

    public SimpleTTLStore() {
        ttlMap = new HashMap<>();
        ttlKeys = new ArrayList<>();
    }

    @Override
    public void setTTL(T key, long expireAt) {
        if (ttlMap.containsKey(key)) {
            // Update existing TTL
            Entry entry = ttlMap.get(key);
            entry.setExpireAt(expireAt);
            return;
        }

        this.ttlKeys.add(key);
        Entry entry = new Entry(expireAt, this.ttlKeys.size() - 1);
        this.ttlMap.put(key, entry);
    }

    @Override
    public long getTTL(T key) {
        if (!ttlMap.containsKey(key)) {
            throw new IllegalArgumentException("Key does not exist in the store");
        }
        return ttlMap.get(key).getExpireAt();
    }

    @Override
    public void removeTTL(T key) {
        if (!ttlMap.containsKey(key)) {
            return;
        }
        Entry entry = ttlMap.get(key);
        int index = entry.getIndex();
        int lastIndex = ttlKeys.size() - 1;
        T lastKey = ttlKeys.get(lastIndex);

        // Swap the key to remove with the last key
        ttlKeys.set(index, lastKey);
        ttlMap.get(lastKey).index = index;

        // Remove the last key
        ttlKeys.remove(lastIndex);
        ttlMap.remove(key);
    }

    @Override
    public boolean isExpired(T key) {
        if (!ttlMap.containsKey(key)) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        return ttlMap.get(key).getExpireAt() <= currentTime;
    }

    @Override
    public T getRandomKeyWithTTL() {
        if (ttlKeys.isEmpty()) {
            return null;
        }
        int randomIndex = (int) (Math.random() * ttlKeys.size());
        return ttlKeys.get(randomIndex);
    }

    @Override
    public int size() {
        return ttlMap.size();
    }

    @Override
    public boolean isEmpty() {
        return ttlMap.isEmpty();
    }
}
