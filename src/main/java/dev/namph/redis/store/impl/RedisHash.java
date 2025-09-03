package dev.namph.redis.store.impl;

import dev.namph.redis.store.RedisValue;

import java.util.List;

public class RedisHash implements RedisValue {
    private final OASet<Entry> set;

    static final class Entry {
        private final Key key;
        private byte[] value;

        public Entry(Key key, byte[] value) {
            this.key = key;
            this.value = value;
        }

        public void setValue(byte[] value) {
            this.value = value;
        }

        public Key getKey() {
            return key;
        }

        public byte[] getValue() {
            return value;
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

    public RedisHash() {
        this.set = new OASet<>();
    }

    public boolean add(byte[] key, byte[] value) {
        Key keyEntry = new Key(key);
        keyEntry.setLastRecentUse();
        Entry entry = new Entry(keyEntry, value);
        if (set.contains(entry)) {
            set.getMember(entry).setValue(value);
        }
        return set.add(entry);
    }

    public boolean remove(byte[] key) {
        return remove(new Key(key));
    }

    public boolean remove(Key key) {
        Entry entry = new Entry(key, null);
        return set.remove(entry);
    }

    public byte[] get(byte[] key) {
        var entry = new Entry(new Key(key), null);
        if (set.contains(entry)) {
            return set.getMember(entry).value;
        }
        return null;
    }

    public int size() {
        return set.size();
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public byte[] getByte() {
        return new byte[0];
    }

    public List<Entry> getAll() {
        return set.getAll();
    }
}
