package dev.namph.redis.store;

import java.util.List;

public interface PersistenceStrategy {
    void save(IStore store, TTLStore ttlStore);
    void load(IStore store);
    void onCommand(List<byte[]> argv, IStore store);
}
