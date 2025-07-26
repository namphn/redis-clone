package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.impl.Key;
import dev.namph.redis.store.impl.RedisString;

import java.util.List;

@Cmd(name = "MGET", minArgs = 2)
public class MGetCommand implements RedisCommand, NeedsStore {
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
        // Prepare a list to hold the values
        List<byte[]> values = new java.util.ArrayList<>();

        // Iterate over the keys provided in the arguments
        for (int i = 1; i < argv.size(); i++) {
            Key key = new Key(argv.get(i));
            var redisValue = store.get(key);
            if (redisValue == null) {
                // If the key does not exist, add a nil response
                values.add(null);
            } else {
                if (!(redisValue instanceof RedisString)) {
                    // If the value is not a string, return an error
                    return encoder.encodeError("WRONG TYPE Operation against a key holding the wrong kind of value");
                }
                // If the key exists, add the value to the list
                values.add(((RedisString) redisValue).getValue());
            }
        }
        // Return the list of values as a multi-bulk response
        return encoder.encodeArray(values);
    }
}
