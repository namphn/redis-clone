package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.util.Singleton;

import java.util.List;

@Cmd(name = "DECR", minArgs = 2)
public class DecrCommand implements RedisCommand, NeedsStore {
    private IStore store;
    private final ProtocolEncoder encoder = Singleton.getResp2Encoder();

    @Override
    public void setStore(IStore store) {
        this.store = store;
    }

    @Override
    public byte[] execute(Connection connection, List<byte[]> argv) {
        if (argv.size() != 2) {
            return encoder.encodeError("ERR wrong number of arguments for 'decr' command");
        }

        byte[] key = argv.get(1);
        byte[] valueBytes = store.get(key);
        long value;

        if (valueBytes == null) {
            // If the key does not exist, initialize it to 0
            value = 0;
        } else {
            try {
                value = Long.parseLong(new String(valueBytes));
            } catch (NumberFormatException e) {
                return encoder.encodeError("ERR value is not an integer or out of range");
            }
        }

        // Decrement the value
        value--;
        store.set(key, String.valueOf(value).getBytes());

        // Return the decremented value as a simple string
        return encoder.encodeInteger(value);
    }
}
