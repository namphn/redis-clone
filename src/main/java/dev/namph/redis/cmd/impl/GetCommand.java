package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.impl.Key;
import dev.namph.redis.store.impl.RedisString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The GET command is used to retrieve the value of a key from the Redis store.
 * If the key does not exist, it returns a nil response.
 * This implementation assumes that the store is already set and available.
 */
@Cmd(name = "GET", minArgs = 2)
public class GetCommand implements RedisCommand, NeedsStore {
    private IStore store;
    private ProtocolEncoder encoder;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void setStore(IStore store) {
        this.store = store;
    }

    @Override
    public void setEncoder(ProtocolEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public byte[] execute(Connection connection, List<byte[]> argv) {
        // Retrieve the value from the store
        logger.info("GET key: {}", new String(argv.get(1)));
        var redisValue = store.get(argv.get(1));

        if (redisValue == null) {
            // If the key does not exist, return a nil response
            return encoder.encodeNil();
        }

        if (!(redisValue instanceof RedisString)) {
            logger.error("WRONG TYPE Operation against a key holding the wrong kind of value");
            return encoder.encodeError("WRONG TYPE Operation against a key holding the wrong kind of value");
        }
        // If the key exists, return the value as a bulk string
        return encoder.encodeBulkString(((RedisString) redisValue).getValue());
    }
}
