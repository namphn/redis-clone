package dev.namph.redis.store.impl;

import dev.namph.redis.store.IStore;
import dev.namph.redis.store.TTLStore;
import org.slf4j.Logger;

public class TTLManager {
    private final TTLStore<Key> ttlStore;
    private final IStore store;
    private final int SAMPLE_SIZE = 20;
    private final int MAX_CYCLE = 5;
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    public TTLManager(TTLStore<Key> ttlStore, IStore store) {
        this.ttlStore = ttlStore;
        this.store = store;
    }

    public void activeExpireCycle() {
        if (ttlStore.isEmpty()) {
            return;
        }

        int cycle = 0;
        while (cycle++ < MAX_CYCLE) {
            int expired = 0;
            int checked = 0;

            if (ttlStore.isEmpty()) {
                break;
            }

            for (int i = 0; i < SAMPLE_SIZE; i++) {
                Key key = ttlStore.getRandomKeyWithTTL();
                if (key == null) {
                    break;
                }
                if (ttlStore.isExpired(key)) {
                    store.remove(key.getVal());
                    ttlStore.removeTTL(key);
                    expired++;
                    logger.warn("Remove key: {}", new String(key.getVal()));
                }
                checked++;
            }
            if (expired == 0) {
                break;
            }

            // if more than 25% keys are expired, continue cycle
            if (checked == 0 ||  ((double) expired / checked) < 0.25) {
                break;
            }
            cycle++;
        }
    }
}
