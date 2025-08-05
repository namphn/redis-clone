package dev.namph.redis.cmd;

import dev.namph.redis.store.TTLStore;

public interface NeedsTTLStore<T> {
    void setTTLStore(TTLStore<T> store);
}
