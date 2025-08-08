package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.impl.Key;
import dev.namph.redis.store.impl.QuickList;

import java.util.List;

@Cmd(name = "LLEN", minArgs = 2)
public class LLenCommand implements RedisCommand, NeedsStore {
    IStore store;
    ProtocolEncoder encoder;

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
        if (argv.size() < 2) {
            return encoder.encodeError("ERR wrong number of arguments for 'LLEN' command");
        }

        var value = store.get(argv.get(1));

        if (value == null) {
            return encoder.encodeInteger(0);
        }

        if (!(value instanceof QuickList list)) {
            return encoder.encodeError("WRONG TYPE Operation against a key holding the wrong kind of value");
        }

        // Return the length of the list
        return encoder.encodeInteger(list.size());
    }
}
