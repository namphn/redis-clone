package dev.namph.redis.store.impl;

import dev.namph.redis.store.DirtyTracker;

public class SimpleDirtyTracker implements DirtyTracker {
    private long dirty = 0;
    @Override
    public void incrDirty() {
        dirty++;
    }

    public long getDirty() {
        return dirty;
    }

    public void reset() {
        dirty = 0;
    }
}
