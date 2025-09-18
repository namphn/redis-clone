package dev.namph.redis.store;

public interface DirtyTracker {
    void incrDirty();
    long getDirty();
    void reset();
}
