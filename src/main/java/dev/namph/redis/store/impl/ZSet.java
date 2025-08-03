package dev.namph.redis.store.impl;

import dev.namph.redis.store.RedisValue;
import java.util.List;

public class ZSet implements RedisValue {
    private final OASet<Entry> set;
    private final SkipList<Key> sortedSet;

    public ZSet() {
        this.set = new OASet<>();
        this.sortedSet = new SkipList<>();
    }

    public static class Entry {
        private final Key key;
        private final double score;

        public Entry(byte[] key, double score) {
            this(new Key(key), score);
        }

        public Entry(Key key, double score) {
            this.key = key;
            this.score = score;
        }

        public Key getKey() {
            return key;
        }

        public double getScore() {
            return score;
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
    }

    public boolean add(byte[] key, double score) {
        return this.add(new Entry(key, score));
    }

    public boolean add(Entry entry) {
        if (entry == null || entry.getKey() == null) {
            return false;
        }
        // Check if the key already exists and has a different score -> need to update
        if (set.contains(entry) && set.getMember(entry).score != entry.score) {
            sortedSet.remove(entry.getKey(), entry.score);
            set.remove(entry);
        }

        if (set.add(entry)) {
            sortedSet.add(entry.getKey(), entry.score);
            return true;
        }

        return false;
    }

    public void remove(Entry entry) {
        if (entry == null) {
            return;
        }
        if (!set.contains(entry)) {
            return;
        }
        if (set.remove(entry)) {
            sortedSet.remove(new Key(entry.getKey().getVal()), entry.getScore() );
        }
    }

    public boolean contains(Entry entry) {
        return set.contains(entry);
    }

    public Entry get(byte[] key) {
        return get(new Entry(new Key(key), 0));
    }

    public Entry get(Entry entry) {
        if (set.contains(entry)) {
            return set.getMember(entry);
        }
        return null;
    }

    public List<Entry> getRangeByRank(int start, int end, int limit, int offset) {
        return sortedSet.getByRank(start, end, limit, offset).stream().parallel().map(node -> {
            return new Entry(node.getKey(), node.getScore());
        }).toList();
    }

    public List<Entry> getRangeByScore(double minScore, double maxScore, int limit, int offset) {
        return sortedSet.getByScore(minScore, maxScore, limit, offset).stream().parallel().map(node -> {
            return new Entry(node.getKey(), node.getScore());
        }).toList();
    }

    public List<Entry> getRangeByScoreReversed(double minScore, double maxScore, int limit, int offset) {
        return sortedSet.getReverseByScore(minScore, maxScore, limit, offset).stream().parallel().map(node -> {
            return new Entry(node.getKey(), node.getScore());
        }).toList();
    }

    public List<Entry> getRangeByLex(byte[] start, byte[] end, int limit, int offset, boolean includeStart, boolean includeEnd) {
        return sortedSet.getByAlphabetical(new Key(start), new Key(end), limit, offset, includeStart, includeEnd).stream().parallel().map(node -> {
            return new Entry(node.getKey(), node.getScore());
        }).toList();
    }

    public List<Entry> getRangeByLexReversed(byte[] start, byte[] end, int limit, int offset, boolean includeStart, boolean includeEnd) {
        return sortedSet.getReverseByAlphabetical(new Key(start), new Key(end), limit, offset, includeStart, includeEnd).stream().parallel().map(node -> {
            return new Entry(node.getKey(), node.getScore());
        }).toList();
    }



    @Override
    public Type getType() {
        return Type.ZSET;
    }
}
