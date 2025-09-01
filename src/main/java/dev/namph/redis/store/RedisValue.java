package dev.namph.redis.store;

public interface RedisValue {
    enum Type {
        STRING,
        LIST,
        SET,
        ZSET,
        HASH
    }
    Type getType();
    byte[] getByte();
}
