package dev.namph.redis.cmd;

import dev.namph.redis.store.IStore;

/**
 * Interface for commands that require a store to be set.
 * This is typically used for commands that need to access or modify a specific store.
 */
public interface NeedsStore {
    void setStore(IStore store);
}
