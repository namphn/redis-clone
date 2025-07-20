package dev.namph.redis.store.impl;

import java.util.Arrays;

public class Key {
    private final byte[] key;
    private final int hash;

    public Key(byte[] key) {
        this.key = key.clone();
        this.hash = Arrays.hashCode(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Key other)) return false;
        return Arrays.equals(key, other.key);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
