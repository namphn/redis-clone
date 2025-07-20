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
    private int mark = -1;

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

    /**
     * Reads a string from the queue.
     * It reads all available bytes and returns them as a string.
     * If there are no bytes to read, it returns null.
     *
     * @return the string read from the queue or null if no data is available
     */
    public String readString() {
        if (read >= write) {
            return null; // No data to read
        }
        int length = write - read;
        return new String(buffer, read, length);
    }

    /**
     * @return the number of bytes available to read from the queue.
     */
    public int readAvailable() {
        return write - read;
    }

    public byte readByte() {
        if (read >= write) {
            throw new IndexOutOfBoundsException("No bytes available to read");
        }
        return buffer[read++];
    }

    /**
     * Reads a specified number of bytes from the queue.
     * If there are not enough bytes available, it throws an exception.
     *
     * @param length the number of bytes to read
     * @return a byte array containing the read bytes
     */
    public byte[] readBytes(int length) {
        if (read + length > write) {
            throw new IndexOutOfBoundsException("Not enough bytes available to read");
        }
        byte[] bytes = Arrays.copyOfRange(buffer, read, read + length);
        read += length;
        return bytes;
    }

    /**
     * Reads a line from the queue.
     * A line is defined as a sequence of bytes ending with CRLF (\r\n).
     * If no complete line is available, it returns null.
     *
     * @return a byte array containing the line or null if no complete line is available
     */
    public byte[] readLine() {
        if (read >= write) {
            return null; // No data to read
        }
        for (int i = read; i < write; i++) {
            if (buffer[i] == '\r' && i + 1 < write && buffer[i + 1] == '\n') {
                // Found CRLF, read up to this point
                byte[] line = Arrays.copyOfRange(buffer, read, i);
                read = i + 2; // Move read pointer past CRLF
                compactIfNeeded();
                return line;
            }
        }
        return null;
    }

    /**
     * Compacts the buffer if necessary.
     */
    private void compactIfNeeded() {
        // Compact the buffer if the read pointer is more than half of the buffer size
        if (read > 0 && (read > buffer.length / 2 || write == buffer.length)) {
            // Move remaining bytes to the start of the buffer
            System.arraycopy(buffer, read, buffer, 0, write - read);
            write -= read;
            read = 0;
        }
    }

    /**
     * Peeks at the next byte in the queue without removing it.
     * @return the next byte in the queue
     */
    public byte peek() {
        if (read >= write) {
            throw new IndexOutOfBoundsException("No bytes available to peek");
        }
        return buffer[read];
    }

    /**
     * Marks the current read position.
     * If a mark is already set, it throws an IllegalStateException.
     */
    public void mark() {
        mark = read;
    }

    /**
     * Resets the read position to the last marked position.
     * If no mark is set, it throws an IllegalStateException.
     */
    public void resetToMark() {
        if (mark < 0) {
            throw new IllegalStateException("No mark set");
        }
        read = mark;
        mark = -1; // Reset the mark after using it
    }
}
