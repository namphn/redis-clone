package dev.namph.redis.store.impl;

import dev.namph.redis.store.EvictionPolicy;
import dev.namph.redis.store.IStore;
import org.slf4j.Logger;

public class MemoryManager {
    private EvictionPolicy<Key> evictionPolicy;
    private final IStore store;
    private int limitMemory;
    private final int DEFAULT_LIMIT_MEMORY = 512;
    private static final long MB = 1024 * 1024;
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    public MemoryManager(EvictionPolicy<Key> evictionPolicy, IStore store) {
        this.evictionPolicy = evictionPolicy;
        this.limitMemory = DEFAULT_LIMIT_MEMORY;
        this.store = store;
    }

    public void setEvictionPolicy(EvictionPolicy<Key> evictionPolicy) {
        this.evictionPolicy = evictionPolicy;
    }

    public void freeMemoryIfNeeded() {
        int usedMemory = calculateUsedMemory();

        while (usedMemory >= limitMemory) {
            logger.warn("used memory: {} over limit: {}", usedMemory, limitMemory);
            var key = evictionPolicy.selectEvictionCandidate();
            store.remove(key.getVal());
            logger.info("Remove key: {}", new String(key.getVal()));
            usedMemory = calculateUsedMemory();
        }
    }

    private int calculateUsedMemory() {
        Runtime rt = Runtime.getRuntime();
        long used = rt.totalMemory() - rt.freeMemory(); // bytes
        return (int) (used / MB);
    }
}
