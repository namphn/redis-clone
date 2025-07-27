package dev.namph.redis.store.impl;

import java.util.Arrays;

public class Key {
    private final byte[] value;
    private final int hash;

    public Key(byte[] key) {
        this.value = key;
        this.hash = Arrays.hashCode(key);
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
}
