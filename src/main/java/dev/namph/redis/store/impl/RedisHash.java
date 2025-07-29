package dev.namph.redis.store.impl;

import dev.namph.redis.store.RedisValue;

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
        Entry entry = new Entry(new Key(key), value);
        return set.add(entry);
    }

    public boolean remove(byte[] key) {
        return set.remove(new Entry(new Key(key), null));
    }

    public byte[] get(byte[] key) {
        var entry = new Entry(new Key(key), null);
        if (set.contains(entry)) {
            return set.getMember(entry).value;
        }
        return null;
    }

    @Override
    public Type getType() {
        return null;
    }
}
