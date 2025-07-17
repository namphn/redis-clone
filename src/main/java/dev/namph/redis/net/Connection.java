package dev.namph.redis.net;

import dev.namph.redis.resp.ByteQueue;
import org.slf4j.Logger;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

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

    /**
     * Constructor for Connection.
     * @param key the SelectionKey associated with this connection.
     * @param channel the SocketChannel associated with this connection.
     */
    public Connection(SelectionKey key, SocketChannel channel) {
        this.key = key;
        this.channel = channel;
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
        logger.info("Read " + n + " bytes from " + channel.getRemoteAddress());
        logger.info("Queue value: " + byteQueue.readString());
    }

    /**
     * Closes the connection.
     * This method is called when the channel is ready for writing.
     */
    private void closeConnection() throws IOException {
        logger.info("Closing connection to " + channel.getRemoteAddress());
        channel.close();
        key.cancel();
    }
}
