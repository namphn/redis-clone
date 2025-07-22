package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.util.Singleton;

import java.util.List;

@Cmd(name = "MGET", minArgs = 2)
public class MgetCommand implements RedisCommand, NeedsStore {
    private IStore store;
    private final ProtocolEncoder encoder = Singleton.getResp2Encoder();

    @Override
    public void setStore(IStore store) {
        this.store = store;
    }

    @Override
    public byte[] execute(Connection connection, List<byte[]> argv) {
        if (argv.size() < 2) {
            return encoder.encodeError("ERR wrong number of arguments for 'mget' command");
        }

        // Prepare a list to hold the values
        List<byte[]> values = new java.util.ArrayList<>();

        // Iterate over the keys provided in the arguments
        for (int i = 1; i < argv.size(); i++) {
            byte[] key = argv.get(i);
            byte[] value = store.get(key);
            if (value == null) {
                // If the key does not exist, add a nil response
                values.add(null);
            } else {
                // If the key exists, add the value to the list
                values.add(value);
            }
        }
        // Return the list of values as a multi-bulk response
        return encoder.encodeArray(values);
    }
}
