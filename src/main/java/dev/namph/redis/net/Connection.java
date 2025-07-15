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

    public void onReadable() throws IOException {
        int n;
        ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        ByteQueue queue = new ByteQueue(BYTE_QUEUE_SIZE);
        while ((n = channel.read(readBuffer)) > 0) {
            readBuffer.flip(); // Prepare buffer for reading
            queue.append(readBuffer);
            readBuffer.clear(); // Clear buffer for next read
        }
        logger.info("Read " + n + " bytes from " + channel.getRemoteAddress());
        logger.info("Queue value: " + queue.readString());
    }
}
