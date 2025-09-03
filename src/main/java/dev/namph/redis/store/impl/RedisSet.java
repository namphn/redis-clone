package dev.namph.redis.store.impl;

import dev.namph.redis.store.RedisValue;

import java.util.List;

public class RedisSet implements RedisValue {
    private final OASet<Key> set ;

    public RedisSet() {
        this.set = new OASet<>();
    }

    public boolean add(Key key) {
        if (set.contains(key)) {
            return false; // Key already exists in the set
        }
        set.add(key);
        return true; // Key added successfully
    }

    public boolean remove(Key key) {
        if (!set.contains(key)) {
            return false; // Key does not exist in the set
        }
        set.remove(key);
        return true; // Key removed successfully
    }

    public boolean contains(Key key) {
        return set.contains(key);
    }

    public int size() {
        return set.size();
    }

    public boolean isEmpty() {
        return set.isEmpty();
    }

    public void clear() {
        set.clear();
    }

    public Key random() {
        return set.randomOneMember();
    }

    public List<Key> random(int count) {
        return set.randomMembers(count);
    }

    public List<Key> getAll() {
        return set.getAll();
    }

    @Override
    public Type getType() {
        return Type.SET;
    }

    @Override
    public byte[] getByte() {
        return null;
    }
}
