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

@Cmd(name = "LINDEX", minArgs = 3)
public class LIndexCommand implements RedisCommand, NeedsStore {
    private IStore store;
    private ProtocolEncoder encoder;

    @Override
    public void setEncoder(ProtocolEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public byte[] execute(Connection connection, List<byte[]> argv) {
        if (argv.size() < 3) {
            return encoder.encodeError("ERR wrong number of arguments for 'LINDEX' command");
        }

        long index;
        try {
            index = Long.parseLong(new String(argv.get(2)));
        } catch (NumberFormatException e) {
            return encoder.encodeError("ERR value is not an integer or out of range");
        }

        // Retrieve the list from the store
        var value = store.get(argv.get(1));
        if (value == null ) {
            return encoder.encodeNil();
        }

        if (!(value instanceof QuickList list) ) {
            return encoder.encodeError("WRONG TYPE Operation against a key holding the wrong kind of value");
        }

        if (index < 0 || index >= list.size()) {
            return encoder.encodeNil();
        }

        // Return the element at the specified index
        return encoder.encodeBulkString(list.get(index));
    }

    @Override
    public void setStore(IStore store) {
        this.store = store;
    }
}
