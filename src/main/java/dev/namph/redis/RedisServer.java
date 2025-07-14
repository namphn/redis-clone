package dev.namph.redis;

import org.slf4j.Logger;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

/** * RedisServer class represents a Redis server instance.
 * It encapsulates the channel, port information.
 * The server can be started with the start method.
 */
public class RedisServer {
    private ServerSocketChannel channel;
    private Selector selector;

    private Integer port;
    private static final int DEFAULT_PORT = 6380;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RedisServer.class);

    /**
     * Constructor for RedisServer.
     * @param port the port on which the server will listen to.
     */
    public RedisServer(Integer port) {
        this.port = port != null ? port : DEFAULT_PORT;
    }

    public void start() {
        try {
            selector = Selector.open();
            channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.bind(new java.net.InetSocketAddress(port));
            channel.register(selector, SelectionKey.OP_ACCEPT);

            logger.info("Redis server started on " + port);

            while (true) {
                selector.select(); // Wait for events
                var selectedKeys = selector.selectedKeys();
                var iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) {
                        // Accept a new connection
                        acceptConnection();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to start Redis server on port " + port, e);
            throw new RuntimeException("Failed to start Redis server", e);
        }
    }

    private void acceptConnection() throws IOException {
        var clientChannel = channel.accept();
        if (clientChannel == null) {
            logger.warn("Failed to accept connection: clientChannel is null");
            return;
        }

        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        logger.info("Accepted new connection from " + clientChannel.getRemoteAddress());
    }
}
