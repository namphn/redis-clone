package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.RedisValue;
import dev.namph.redis.store.impl.Key;
import dev.namph.redis.store.impl.RedisString;

import java.util.List;

@Cmd(name = "DECR", minArgs = 2)
public class DecrCommand implements RedisCommand, NeedsStore {
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
            return encoder.encodeError("ERR wrong number of arguments for 'decr' command");
        }

        Key key = new Key(argv.get(1));
        RedisValue redisValue = store.get(key);

        if (redisValue != null && !(redisValue instanceof RedisString)) {
            return encoder.encodeError("ERR WRONG TYPE Operation against a key holding the wrong kind of value");
        }
        long value;

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

        // Decrement the value
        value--;
        store.set(key, new RedisString(value));

        // Return the decremented value as a simple string
        return encoder.encodeInteger(value);
    }
}
