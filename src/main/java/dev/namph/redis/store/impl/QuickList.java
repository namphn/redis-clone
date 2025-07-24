package dev.namph.redis.store.impl;

import dev.namph.redis.store.RedisValue;
import java.util.ArrayDeque;
import java.util.LinkedList;

public class QuickList implements RedisValue {
    // use ArrayDeque as chunk
    private final LinkedList<ArrayDeque<byte[]>> list;
    private static final int MAX_CHUNK_SIZE = 512;
    private static final int MIN_CHUNK_SIZE = MAX_CHUNK_SIZE / 3;
    private static int maxChunkSize; // allow configuration of max chunk size in the future
    private static int minChunkSize; // allow configuration of min chunk size in the future
    private long total = 0;

    public QuickList() {
        this.list = new LinkedList<>();
        maxChunkSize = MAX_CHUNK_SIZE;
        minChunkSize = MIN_CHUNK_SIZE;
    }

    public QuickList(int maxChunkSize, int minChunkSize, LinkedList<ArrayDeque<byte[]>> list) {
        this.list = list;
        QuickList.maxChunkSize = maxChunkSize;
        QuickList.minChunkSize = minChunkSize;
        this.total = 0; // reset total count
    }

    public void setMaxChunkSize(int param) {
        maxChunkSize = param;
    }

    public void setMinChunkSize(int param) {
        minChunkSize = param;
    }

    public long addFirst(byte[] value) {
        if (list.isEmpty() || list.getFirst().size() >= maxChunkSize) {
            list.addFirst(new ArrayDeque<>());
        }
        list.getFirst().addFirst(value);
        total++;

        return total;
    }

    public long addLast(byte[] value) {
        if (list.isEmpty() || list.getLast().size() >= maxChunkSize) {
            list.addLast(new ArrayDeque<>());
        }
        list.getLast().addLast(value);
        total++;
        return total;
    }

    public byte[] getFirst() {
        if (list.isEmpty()) {
            return null; // or throw an exception
        }
        ArrayDeque<byte[]> firstChunk = list.getFirst();
        if (firstChunk.isEmpty()) {
            return null; // or throw an exception
        }
        return firstChunk.getFirst();
    }

    public byte[] getLast() {
        if (list.isEmpty()) {
            return null; // or throw an exception
        }
        ArrayDeque<byte[]> lastChunk = list.getLast();
        if (lastChunk.isEmpty()) {
            return null; // or throw an exception
        }
        return lastChunk.getLast();
    }

    public byte[] removeFirst() {
        if (list.isEmpty()) {
            return null; // or throw an exception
        }
        ArrayDeque<byte[]> firstChunk = list.getFirst();
        if (firstChunk.isEmpty()) {
            list.removeFirst(); // remove empty chunk
            return null; // or throw an exception
        }
        byte[] value = firstChunk.removeFirst();
        if (firstChunk.isEmpty()) {
            list.removeFirst(); // remove empty chunk
        }
        total--;
        if (list.getFirst().size() < minChunkSize) {
            mergeFirst();
        }
        return value;
    }

    private void mergeFirst() {
        if (list.size() < 2) {
            return; // nothing to merge
        }
        ArrayDeque<byte[]> firstChunk = list.getFirst();
        ArrayDeque<byte[]> secondChunk = list.get(1);
        firstChunk.addAll(secondChunk);
        list.remove(1); // remove the second chunk
        if (firstChunk.size() < minChunkSize) {
            mergeFirst(); // recursively merge if still below min size
        }
    }

    public byte[] removeLast() {
        if (list.isEmpty()) {
            return null; // or throw an exception
        }
        ArrayDeque<byte[]> lastChunk = list.getLast();
        if (lastChunk.isEmpty()) {
            list.removeLast(); // remove empty chunk
            return null; // or throw an exception
        }
        byte[] value = lastChunk.removeLast();
        if (lastChunk.isEmpty()) {
            list.removeLast(); // remove empty chunk
        }
        total--;
        if (list.getLast().size() < minChunkSize) {
            mergeLast();
        }
        return value;
    }

    private void mergeLast() {
        if (list.size() < 2) {
            return; // nothing to merge
        }
        ArrayDeque<byte[]> lastChunk = list.getLast();
        ArrayDeque<byte[]> secondLastChunk = list.get(list.size() - 2);
        secondLastChunk.addAll(lastChunk);
        list.removeLast(); // remove the last chunk
        if (secondLastChunk.size() < minChunkSize) {
            mergeLast(); // recursively merge if still below min size
        }
    }

    @Override
    public Type getType() {
        return Type.LIST;
    }
}
