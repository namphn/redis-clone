package dev.namph.redis.store.impl;

import dev.namph.redis.store.RedisValue;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
        while (!list.isEmpty() && list.getFirst().isEmpty()) {
            list.removeFirst(); // remove empty chunk
        }

        if (list.isEmpty()) {
            return null; // or throw an exception
        }

        var firstChunk = list.getFirst();

        byte[] value = firstChunk.removeFirst();
        if (firstChunk.isEmpty()) {
            list.removeFirst(); // remove empty chunk
        }
        total--;
        if (!list.isEmpty() && list.getFirst().size() < minChunkSize) {
            mergeFirst();
        }
        return value;
    }

    public List<byte[]> removeFirst(long count) {
        if (list.isEmpty()) {
            return null;
        }

        List<byte[]> values = new ArrayList<>();

        while (count > 0 && !list.isEmpty()) {
            if (list.getFirst().isEmpty()) {
                list.removeFirst(); // remove empty chunk
            }
            if (list.isEmpty()) {
                break; // no more chunks to process
            }
            values.add(list.getFirst().removeFirst());
            count--;
            total--;
        }

        if (!list.isEmpty() && list.getFirst().size() < minChunkSize) {
            mergeFirst();
        }

        return values;
    }

    private void mergeFirst() {
        if (list.size() < 2) {
            return; // nothing to merge
        }
        if (list.getFirst().size() + list.get(1).size() >= maxChunkSize) {
            return;
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

    public boolean isEmpty() {
        return total == 0;
    }

    @Override
    public Type getType() {
        return Type.LIST;
    }
}
