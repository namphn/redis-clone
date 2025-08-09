package dev.namph.redis.cmd;

import dev.namph.redis.store.impl.MemoryManager;

public interface NeedMemoryManager {
    void setMemoryManager(MemoryManager memoryManager);
}
