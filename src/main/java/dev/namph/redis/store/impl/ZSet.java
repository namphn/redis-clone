package dev.namph.redis.store.impl;

import dev.namph.redis.store.RedisValue;

import java.util.NavigableSet;
import java.util.TreeSet;

public class ZSet implements RedisValue {
    private final OASet<Entry> set;
    private final NavigableSet<Entry> sortedSet;

    public ZSet() {
        this.set = new OASet<>();
        this.sortedSet = new TreeSet<>();
    }

    static final class Entry implements Comparable<Entry> {
        private final Key key;
        private double score;

        public Entry(Key key, double score) {
            this.key = key;
            this.score = score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Entry other)) return false;
            return key.equals(other.key);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public int compareTo(Entry o) {
            int cmp = Double.compare(this.score, o.score);
            if (cmp == 0) return this.key.hashCode() - o.key.hashCode();
            return cmp;
        }
    }

    public boolean add(byte[] key, double score) {
        Entry entry = new Entry(new Key(key), score);
        if (set.add(entry)) {
            sortedSet.add(entry);
            return true;
        }
        return false;
    }

    public boolean remove(byte[] key) {
        Entry entry = new Entry(new Key(key), 0);
        if (set.remove(entry)) {
            sortedSet.remove(entry);
            return true;
        }
        return false;
    }

    @Override
    public Type getType() {
        return Type.ZSET;
    }
}
