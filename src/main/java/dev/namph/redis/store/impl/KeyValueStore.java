package dev.namph.redis.store.impl;

import dev.namph.redis.store.IStore;
import dev.namph.redis.store.RedisValue;
import dev.namph.redis.store.TTLStore;

public class KeyValueStore implements IStore {
    private final OASet<Entry> kv;
    private final TTLStore<Key> ttlStore;

    public static class Entry {
        public final Key key;
        public final RedisValue value;

        public Entry(byte[] key, RedisValue value) {
            this(new Key(key), value);
        }

        public Entry(Key key, RedisValue value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Entry)) return false;
            Entry other = (Entry) obj;
            return key.equals(other.key);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

    public KeyValueStore(TTLStore<Key> ttlStore) {
        this.kv = new OASet<>();
        this.ttlStore = ttlStore;
    }

    @Override
    public void set(byte[] key, RedisValue value) {
        Entry entry = new Entry(key, value);
        kv.add(entry);
    }

    @Override
    public RedisValue get(byte[] key) {
        Key keyObj = new Key(key);
        Entry entry = new Entry(keyObj, null);
        if (!kv.contains(entry)) {
            return null;
        }
        if (ttlStore.isExpired(keyObj)) {
            kv.remove(keyObj);
            ttlStore.removeTTL(keyObj);
            return null;
        }

        entry = kv.getMember(entry);
        entry.key.setLastRecentUse();
        return entry.value;
    }

    @Override
    public int size() {
        return kv.size();
    }

    @Override
    public void remove(byte[] key) {
        kv.remove(new Entry(key, null));
    }

    public void remove(Key key) {
        kv.remove(key);
        ttlStore.removeTTL(key);
    }
}
