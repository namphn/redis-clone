package dev.namph.redis.store.impl;

import dev.namph.redis.store.IStore;
import dev.namph.redis.store.RedisValue;
import dev.namph.redis.store.TTLStore;

import java.util.List;
import java.util.Map;

public class KeyValueStore implements IStore {
    private OASet<Entry> kv;
    private TTLStore<Key> ttlStore;

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

    @Override
    public Key getRandomKey() {
        if (kv.size() == 0) {
            return null;
        }
        Entry entry = kv.randomOneMember();
        return entry == null ? null : entry.key;
    }

    @Override
    public boolean contains(byte[] key) {
        return kv.contains(new Entry(key, null));
    }

    @Override
    public Entry[] getAll() {
       List<Entry> res = kv.getAll();
       return res.toArray(new Entry[res.size()]);
    }

    private void setKeyValueStore(OASet<Entry> kv) {
        this.kv = kv;
    }

    @Override
    public IStore clone() {
        SimpleTTLStore<Key> cloneTTl = new SimpleTTLStore<>();
        for (Map.Entry<Key, SimpleTTLStore.Entry> entry : ((SimpleTTLStore<Key>) ttlStore).getAll()) {
            var key = new Key(entry.getKey());
            cloneTTl.setTTL(key, entry.getValue().getExpireAt());
        }
        KeyValueStore cloneStore = new KeyValueStore(cloneTTl);
        cloneStore.setKeyValueStore(kv.clone());
        return cloneStore;
    }

    @Override
    public TTLStore getTTLStore() {
        return ttlStore;
    }

    @Override
    public boolean isExpired(Key key) {
        return ttlStore.isExpired(key);
    }

    @Override
    public long getTTL(Key key) {
        return ttlStore.getTTL(key);
    }


}
