package dev.namph.redis;

import dev.namph.redis.cmd.impl.CommandRegistry;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.EvictionPolicy;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.TTLStore;
import dev.namph.redis.store.impl.*;
import dev.namph.redis.util.Singleton;
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

    private final Integer port;
    private static final int DEFAULT_PORT = 6380;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RedisServer.class);
    private final IStore store;
    private final TTLStore<Key> ttlStore;
    private final ProtocolEncoder encoder;
    private final CommandRegistry commandRegistry;
    private final long CLEANUP_INTERVAL_MS = 100; // 100ms
    private final TTLManager ttlManager;

    /**
     * Constructor for RedisServer.
     * @param port the port on which the server will listen to.
     */
    public RedisServer(Integer port) {
        this.port = port != null ? port : DEFAULT_PORT;
        ttlStore = new SimpleTTLStore<>();
        store = new KeyValueStore(ttlStore);
        encoder = Singleton.getResp2Encoder();
        EvictionPolicy<Key> evictionPolicy = new AllKeysLRU(store);
        MemoryManager memoryManager = new MemoryManager(evictionPolicy, store);
        commandRegistry = new CommandRegistry(store, encoder, ttlStore, memoryManager);
        ttlManager = new TTLManager(ttlStore, store);
    }

    /**
     * Starts the Redis server.
     * It opens a selector and a server socket channel, binds to the specified port,
     * and listens for incoming connections.
     */
    public void start() {
        try {
            selector = Selector.open();
            channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.bind(new java.net.InetSocketAddress(port));
            channel.register(selector, SelectionKey.OP_ACCEPT);

            logger.info("Redis server started on " + port);

            long lastCleanupTime = System.currentTimeMillis();
            while (true) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastCleanupTime >= CLEANUP_INTERVAL_MS) {
                    ttlManager.activeExpireCycle();
                    lastCleanupTime = currentTime;
                }
                selector.select((int) CLEANUP_INTERVAL_MS); // Wait for events
                var selectedKeys = selector.selectedKeys();
                var iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (!key.isValid()) continue;

                    // Accept a new connection
                    if (key.isAcceptable()) {
                        acceptConnection();
                    }

                    // Handle readable connections
                    if (key.isValid() && key.isReadable()) {
                        Connection connection = (Connection) key.attachment();
                        if (connection != null) {
                            try {
                                connection.onReadable();
                            } catch (IOException e) {
                                logger.error("Error reading from connection", e);
                                key.cancel(); // Cancel the key if there's an error
                            }
                        } else {
                            logger.warn("Connection attachment is null for key: " + key);
                        }
                    }

                    // Handle writable connections
                    if (key.isValid() && key.isWritable()) {
                        Connection connection = (Connection) key.attachment();
                        if (connection != null) {
                            try {
                                connection.onWritable();
                            } catch (IOException e) {
                                logger.error("Error writing to connection", e);
                                key.cancel(); // Cancel the key if there's an error
                            }
                        } else {
                            logger.warn("Connection attachment is null for key: " + key);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to start Redis server on port " + port, e);
            throw new RuntimeException("Failed to start Redis server", e);
        }
    }

    /**
     * Accepts a new connection from a client.
     * Configures the client channel to non-blocking mode and registers it with the selector.
     */
    private void acceptConnection() throws IOException {
        var clientChannel = channel.accept();
        if (clientChannel == null) {
            logger.warn("Failed to accept connection: clientChannel is null");
            return;
        }

        clientChannel.configureBlocking(false);
        SelectionKey selectionKey = clientChannel.register(selector, SelectionKey.OP_READ);
        Connection connection = new Connection(selectionKey, clientChannel, commandRegistry);
        selectionKey.attach(connection);
    }
}
