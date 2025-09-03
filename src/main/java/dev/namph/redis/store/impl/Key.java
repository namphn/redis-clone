package dev.namph.redis.store.impl;

import java.util.Arrays;

public class Key {
    private final byte[] value;
    private final int hash;
    private final int RESOLUTION = 100; // 100ms resolution
    int lastRecentUse;

    public Key(byte[] key) {
        this.value = key;
        this.hash = Arrays.hashCode(key);
    }

    public Key(Key key) {
        this.value = key.value.clone();
        this.hash = key.hash;
        this.lastRecentUse = key.lastRecentUse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Key other)) return false;
        return Arrays.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public byte[] getVal() {
        return value;
    }

    public void setLastRecentUse() {
        this.lastRecentUse = calculateLRUClock();
    }

    private int calculateLRUClock() {
        long now = System.currentTimeMillis() / RESOLUTION;
        return (int) (now & 0xFFFFFFFFL); // keep 24 bits
    }

    public int getAge() {
        int lruClock = calculateLRUClock();
        return (lruClock - lastRecentUse) & 0xFFFFFF;
    }
}
