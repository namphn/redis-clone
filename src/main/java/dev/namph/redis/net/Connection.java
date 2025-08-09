package dev.namph.redis.net;

import dev.namph.redis.cmd.impl.CommandRegistry;
import dev.namph.redis.resp.*;
import org.slf4j.Logger;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;

public class Connection {
    private SelectionKey key;
    private SocketChannel channel;
    private ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE); // 8KB read buffer
    private ByteQueue byteQueue = new ByteQueue(BYTE_QUEUE_SIZE); // ByteQueue to hold read data

    // allocated 8KB = 2 blocks of 4KB each, which is a common size for network buffers
    private static final int BUFFER_SIZE = 8192;
    // size of the byte queue. double the buffer size to accommodate larger data transfers
    private static final int BYTE_QUEUE_SIZE = 8192 * 2;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Connection.class);
    private ProtocolParser parser;
    private Deque<ByteBuffer> writeQueue; // Queue for write operations
    private ProtocolEncoder encoder;
    private CommandRegistry commandRegistry;
    private boolean closeAfterWrite = false;

    /**
     * Constructor for Connection.
     * @param key the SelectionKey associated with this connection.
     * @param channel the SocketChannel associated with this connection.
     * @param commandRegistry the CommandRegistry to handle commands for this connection.
     */
    public Connection(SelectionKey key, SocketChannel channel, CommandRegistry commandRegistry) {
        this.key = key;
        this.channel = channel;
        parser = new Resp2Parser();
        writeQueue = new ArrayDeque<>();
        encoder = new Resp2Encoder();
        this.commandRegistry = commandRegistry;
    }

    /**
     * Handles the readable event for this connection.
     * This method reads data from the channel into the read buffer,
     * appends it to the byte queue, and processes the data.
     *
     * @throws IOException if an I/O error occurs while reading from the channel.
     */
    public void onReadable() throws IOException {
        int n;
        while ((n = channel.read(readBuffer)) > 0) {
            readBuffer.flip(); // Prepare buffer for reading
            byteQueue.append(readBuffer);
            readBuffer.clear(); // Clear buffer for next read
        }
        if (n == -1) {
            closeConnection();
            return;
        }

        // parse and process the command
        while (true) {
            var parseResult = parser.tryParseCommand(byteQueue);
            switch (parseResult.type()) {
                case IM_COMPLETE -> {
                    return; // Incomplete command, wait for more data
                }
                case ERROR -> {
                    // Handle error in parsing
                    logger.error("Error parsing command: " + parseResult.message());
                    var errorResponse = encoder.encodeError(parseResult.message());
                    enqueueWrite(errorResponse);
                    enableWrite(); // Enable write operation to send the error response
                    closeAfterWrite = true; // Close after writing the error response
                    return;
                }
                case COMMAND -> {
                    // Process the command
                    byte[] res = commandRegistry.dispatch(this, parseResult.args());
                    enqueueWrite(res);
                    enableWrite(); // Enable write operation to send the response
                }
            }
        }
    }

    /**
     * Handles the writable event for this connection.
     * This method writes data from the write queue to the channel.
     *
     * @throws IOException if an I/O error occurs while writing to the channel.
     */
    public void onWritable() throws IOException {
        if (writeQueue.isEmpty()) {
            disableWrite(); // Disable write operation if there's nothing to write
            if (closeAfterWrite) {
                closeConnection(); // Close the connection if we are done writing
            }
            return;
        }

        while (!writeQueue.isEmpty()) {
            ByteBuffer buffer = writeQueue.peek();
            if (buffer == null || !buffer.hasRemaining()) {
                writeQueue.poll(); // Remove empty buffers
                continue;
            }
            int bytesWritten = channel.write(buffer);
            if (bytesWritten <= 0) {
                break; // No more data can be written at the moment
            }
        }
    }

    /**
     * Closes the connection.
     * This method is called when the channel is ready for writing.
     */
    private void closeConnection() throws IOException {
        logger.info("Closing connection to " + channel.getRemoteAddress());
        try {
            channel.close();
            key.cancel();
        } catch (Exception e) {
            logger.error("Error closing connection to " + channel.getRemoteAddress(), e);
        }
    }

    /**
     * Enables the write operation for this connection.
     * This method sets the interest ops of the key to include OP_WRITE
     * and wakes up the selector to process the write operation.
     */
    private void enableWrite() {
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        key.selector().wakeup();
    }

    /**
     * Enqueues a byte array for writing.
     * This method wraps the byte array in a ByteBuffer and adds it to the write queue.
     *
     * @param bytes the byte array to enqueue for writing.
     * @throws IOException if an I/O error occurs while enqueuing the write operation.
     */
    private void enqueueWrite(byte[] bytes) throws IOException {
        enqueueWrite(ByteBuffer.wrap(bytes));
    }

    /**
     * Enqueues a ByteBuffer for writing.
     * This method adds the ByteBuffer to the writing queue if it is not empty.
     *
     * @param buffer the ByteBuffer to enqueue for writing.
     * @throws IOException if an I/O error occurs while enqueuing the write operation.
     */
    private void enqueueWrite(ByteBuffer buffer) throws IOException {
        if (buffer == null || buffer.remaining() == 0) {
            logger.warn("from client" + channel.getRemoteAddress() + ": Attempted to enqueue an empty buffer for writing.");
            return;
        }
        writeQueue.offer(buffer);
    }

    /**
     * Disables the write operation for this connection.
     * This method sets the interest ops of the key to remove OP_WRITE
     * and wakes up the selector to process the change.
     */
    private void disableWrite() {
        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
        key.selector().wakeup();
    }

    /**
     * Sends a QUIT command to the Redis server.
     * This method encodes the QUIT command as a simple string and enqueues it for writing.
     * After sending the command, it enables the write operation and sets closeAfterWrite to true,
     * indicating that the connection should be closed after the write operation is complete.
     */
    public void quit() {
        byte[] quit = encoder.encodeSimpleString("QUIT");
        try {
            enqueueWrite(quit);
        } catch (IOException e) {
            logger.error("Failed to send QUIT command", e);
        }
        enableWrite();
        closeAfterWrite = true;
    }
}
