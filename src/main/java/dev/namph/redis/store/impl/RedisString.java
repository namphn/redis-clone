package dev.namph.redis.store.impl;

import dev.namph.redis.store.RedisValue;

public class RedisString implements RedisValue {
    private byte[] value;

    public RedisString(byte[] value) {
        this.value = value.clone();
    }

    public RedisString(long value) {
        this.value = String.valueOf(value).getBytes();
    }

    public byte[] getValue() {
        return value.clone();
    }

    public void setValue(byte[] value) {
        this.value = value.clone();
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }

    @Override
    public byte[] getByte() {
        return value;
    }

    public String getStringValue() {
        return new String(value);
    }
}
