package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.resp.Resp2Encoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.util.Singleton;

import java.util.List;

@Cmd(name = "SET", minArgs = 3)
public class SetCommand implements RedisCommand, NeedsStore {
    private IStore store;
    private final ProtocolEncoder encoder = Singleton.getResp2Encoder();

    @Override
    public byte[] execute(Connection connection, List<byte[]> argv) {
        if (argv.size() < 3) {
            return "ERR wrong number of arguments for 'set' command".getBytes();
        }

        byte[] key = argv.get(1);
        byte[] value = argv.get(2);

        // Store the key-value pair in the store
        store.set(key, value);

        // Return a simple string response indicating success
        return encoder.encodeSimpleString("OK");
    }

    @Override
    public void setStore(IStore store) {
        this.store = store;
    }
}
