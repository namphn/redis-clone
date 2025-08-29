package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.impl.RedisString;

import java.util.List;

@Cmd(name = "GETRANGE", minArgs = 4)
public class GetRangeCommand implements RedisCommand, NeedsStore {
    private ProtocolEncoder encoder;
    private IStore store;

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
        if (argv.size() > 4) {
            encoder.encodeError("ERR syntax error");
        }
        var val = store.get(argv.get(1));
        if (val == null) {
            encoder.encodeSimpleString("");
        }

        int start = 0, end = 0;
        try {
            start = Integer.parseInt(new String(argv.get(2)));
            end = Integer.parseInt(new String(argv.get(3)));
        } catch (NumberFormatException e) {
            encoder.encodeError("ERR value is not an integer or out of range");
        }

        if (val instanceof RedisString strVal) {
            return encoder.encodeSimpleString((strVal.getStringValue().substring(start, end)));
        }

        return encoder.encodeError("WRONG TYPE Operation against a key holding the wrong kind of value");
    }
}
