package dev.namph.redis.cmd.impl;


import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.impl.Key;
import dev.namph.redis.store.impl.RedisString;
import dev.namph.redis.util.Singleton;

import java.util.List;

@Cmd(name = "INCR", minArgs = 2)
public class IncrCommand implements RedisCommand, NeedsStore {
    private IStore store;
    private ProtocolEncoder encoder;

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
        if (argv.size() != 2) {
            return encoder.encodeError("ERR wrong number of arguments for 'INCR' command");
        }

        var redisValue = store.get(argv.get(1));
        long value;

        // Check if the value is a valid RedisString
        if (redisValue != null && !(redisValue instanceof RedisString)) {
            return encoder.encodeError("ERR WRONG TYPE Operation against a key holding the wrong kind of value");
        }

        if (redisValue == null) {
            // If the key does not exist, initialize it to 0
            value = 0;
        } else {
            try {
                value = Long.parseLong(new String(((RedisString) redisValue).getValue()));
            } catch (NumberFormatException e) {
                return encoder.encodeError("ERR value is not an integer or out of range");
            }
        }

        // Increment the value
        value++;
        store.remove(argv.get(1));
        store.set(argv.get(1), new RedisString(value));

        // Return the incremented value as a simple string
        return encoder.encodeInteger(value);
    }
}
