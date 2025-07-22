package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.util.Singleton;

import java.util.List;

@Cmd(name = "MSET", minArgs = 3)
public class MsetCommand implements RedisCommand, NeedsStore {
    private IStore store;
    private final ProtocolEncoder encoder = Singleton.getResp2Encoder();

    @Override
    public void setStore(IStore store) {
        this.store = store;
    }

    @Override
    public byte[] execute(Connection connection, List<byte[]> argv) {
        if (argv.size() < 3 || argv.size() % 2 != 1) {
            return encoder.encodeError("ERR wrong number of arguments for 'mset' command");
        }

        for (int i = 1; i < argv.size(); i += 2) {
            byte[] key = argv.get(i);
            byte[] value = argv.get(i + 1);
            store.set(key, value);
        }

        return encoder.encodeSimpleString("OK");
    }
}
