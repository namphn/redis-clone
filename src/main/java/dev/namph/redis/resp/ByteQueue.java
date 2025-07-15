package dev.namph.redis.resp;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * ByteQueue is a simple byte buffer that allows appending bytes
 * and ensures that it has enough capacity to hold the data.
 * It dynamically resizes the internal buffer when necessary.
 */
public class ByteQueue {
    private int read;
    private int write;
    private byte[] buffer;
    private static final int DEFAULT_SIZE = 1024;

    /**
     * Default constructor for ByteQueue.
     * Initializes the buffer with a default size.
     */
    public ByteQueue(int capacity) {
        this.buffer = new byte[capacity > 0 ? capacity : DEFAULT_SIZE];
        this.read = 0;
        this.write = 0;
    }

    /**
     * Appends the given byte array to the queue.
     * If the buffer does not have enough capacity, it will be resized.
     *
     * @param buffer the byte array to append
     */
    public void append(ByteBuffer buffer) {
        int remaining = buffer.remaining();
        ensureCapacity(remaining);
        buffer.get(this.buffer, write, remaining);
        write += remaining;
    }

    /**
     * Ensures that the internal buffer has enough capacity
     *
     * @param requiredCapacity the number of bytes to append
     */
    private void ensureCapacity(int requiredCapacity) {
        if (write + requiredCapacity > buffer.length) {
            // Calculate new size, at least double the current size or enough to hold the new data
            int newSize = Math.max(buffer.length * 2, write + requiredCapacity);
            // resize the buffer
            buffer =  Arrays.copyOf(buffer, newSize);
        }
    }

    public String readString() {
        if (read >= write) {
            return null; // No data to read
        }
        int length = write - read;
        String result = new String(buffer, read, length);
        return result;
    }
}
