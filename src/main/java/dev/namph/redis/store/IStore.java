package dev.namph.redis.store;

public interface IStore {
    void set(byte[] key, byte[] value);
    byte[] get(byte[] key);
    int size();
}
