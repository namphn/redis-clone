package dev.namph.redis.store.impl;

import dev.namph.redis.store.RedisValue;

import java.util.*;
import java.util.Set;

public class OASet<T> implements RedisValue {
    // slot marker
    private final Object EMPTY = new Object();
    private final Object TOMB = new Object();

    private int size;
    private int used;
    private final double maxLoad;
    private long modCount; // for concurrency control
    private  Object [] table;

    private static final double DEFAULT_MAX_LOAD = 0.70;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int MIN_CAPACITY = 4;
    private static final int MAX_RANDOM_TRIES = 8;
    private static final int MAX_WINDOW_SCAN = 8;
    private static final SplittableRandom random = new SplittableRandom();

    @Override
    public Type getType() {
        return Type.SET;
    }

    public OASet() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_MAX_LOAD);
    }

    public OASet(int capacity) {
        this(capacity, DEFAULT_MAX_LOAD);
    }

    public OASet(int capacity, double maxLoad) {
        int cap = 1;
        while (cap <= Math.max(MIN_CAPACITY, capacity)) {
            cap <<= 1; // Ensure capacity is a power of two
        }

        this.table = new Object[cap];
        Arrays.fill(this.table, EMPTY); // Initialize all slots to EMPTY
        this.maxLoad = Math.min(Math.max(maxLoad, 0.50), 0.90); // Ensure maxLoad is between 0.50 and 0.90
        this.size = 0;
        this.used = 0;
        this.modCount = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(Object o) {
        return findIndex(o) != -1;
    }

    public boolean add(T o) {
        if (o == null) {
            return false; // Null values are not allowed
        }
        ensureCapacity();
        int index = fMix32(o.hashCode());
        index &= (table.length - 1); // Ensure the index is within bounds

        int firstTombIndex = -1; // Track the first tombstone index
        Object current = table[index];
        while (current != EMPTY) {
            if (current == TOMB) {
                if (firstTombIndex == -1) {
                    firstTombIndex = index; // Remember the first tombstone index
                }
            } else if (current.equals(o)) {
                return false; // Object already exists
            }
            index = (index + 1) & (table.length - 1); // Linear probing
            current = table[index];
        }

        table[index] = o; // Place the new object
        size++;
        used++;
        modCount++;
        return true;
    }

    public List<T> getAll() {
        List<T> result = new ArrayList<>(size);
        for (Object item : table) {
            if (item != EMPTY && item != TOMB) {
                result.add((T) item); // Collect only valid items
            }
        }
        return result;
    }

    public boolean remove(Object o) {
        if (o == null) {
            return false; // Null values are not allowed
        }
        int index = findIndex(o);
        if (index == -1) {
            return false; // Object not found
        }

        table[index] = TOMB; // Mark as tombstone
        size--;
        modCount++;
        return true;
    }

    public void clear() {
        Arrays.fill(table, EMPTY); // Reset all slots to EMPTY
        size = 0;
        used = 0;
        modCount++;
    }

    public T randomOneMember() {
        if (isEmpty()) {
            return null;
        }

        int index = randomOccupiedIndex();
        if (index == -1) {
            return null; // No occupied index found
        }
        return (T) table[index];
    }

    @SuppressWarnings("unchecked")
    public List<T> randomMembers(int count) {
        if (isEmpty()) {
            return Collections.emptyList();
        }

        // random with duplicate members
        if (count <= 0) {
            int turn = -count;
            List<T> result = new ArrayList<>();
            while (turn-- > 0) {
                int index = randomOccupiedIndex();
                if (index == -1) {
                    return result;
                }
                result.add((T) table[index]);
            }
            return result;
        }

        Set<T> result = new HashSet<>();
        while (result.size() < count) {
            int index = randomOccupiedIndex();
            if (index == -1) {
                break; // No more occupied indices available
            }
            result.add((T) table[index]);
        }

        return new ArrayList<>(result);
    }

    private int randomOccupiedIndex() {
        if (isEmpty()) {
            return -1; // No elements to choose from
        }
        int tries = 0;
        int index;
        do {
            index = random.nextInt(0, table.length);
            if (table[index] != EMPTY && table[index] != TOMB) {
                return index; // Found a valid occupied index
            }
            tries++;
        } while (tries < MAX_RANDOM_TRIES);

        // fall back to liner with max window scan
        int start = random.nextInt(0, table.length);
        for (int i = 0; i < MAX_WINDOW_SCAN; i++) {
            index = (start + i) & (table.length - 1);
            if (table[index] != EMPTY && table[index] != TOMB) {
                return index; // Found a valid occupied index
            }
        }

        // full scan if no valid index found
        for (int i = 0; i < table.length; i++) {
            if (table[index] != EMPTY && table[index] != TOMB) {
                return index; // Found a valid occupied index
            }
        }

        return -1; // Failed to find a valid index after max tries
    }

    private int findIndex(Object o) {
        if (o == null) {
            return -1; // Null values are not allowed
        }
        int index = fMix32(o.hashCode()); // mix bits to reduce collisions
        index &= (table.length - 1); // Ensure the index is within bounds

        Object current = table[index];
        while (current != EMPTY) {
            if (current != TOMB && current.equals(o)) {
                return index; // Found the object
            }
            index = (index + 1) & (table.length - 1); // Linear probing
            current = table[index];
        }
        return -1; // Not found
    }

    private void ensureCapacity() {
        if (used >= (table.length * maxLoad)) {
            resize(table.length << 1);
        }
    }

    private void resize(int newCapacity) {
        Object[] oldTable = table;
        table = new Object[newCapacity];
        Arrays.fill(table, EMPTY); // Initialize all slots to EMPTY
        size = 0;
        used = 0;

        for (Object item : oldTable) {
            if (item != EMPTY && item != TOMB) {
                int index = fMix32(item.hashCode());
                index &= (newCapacity - 1); // Ensure the index is within bounds

                while (table[index] != EMPTY) {
                    index = (index + 1) & (newCapacity - 1); // Linear probing
                }
                table[index] = item; // Place the item in the new table
                size++;
                used++;
            }
        }
    }

    private  static int fMix32(int h) {
        // currently use 32 bit to hash, if there is much collision, consider using 64 bit
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }
}
