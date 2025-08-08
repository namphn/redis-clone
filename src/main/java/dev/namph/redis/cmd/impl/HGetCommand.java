package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.impl.Key;
import dev.namph.redis.store.impl.RedisHash;

import java.util.List;

@Cmd(name = "HGET", minArgs = 3)
public class HGetCommand implements RedisCommand, NeedsStore {
    private ProtocolEncoder encoder;
    private IStore store;

    @Override
    public void setEncoder(ProtocolEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public void setStore(IStore store) {
        this.store = store;
    }

    @Override
    public byte[] execute(Connection connection, List<byte[]> argv) {
        if (argv.size() != 3) {
            return encoder.encodeError("ERR wrong number of arguments for 'HGET' command");
        }

        var field = argv.get(2);
        var value = store.get(argv.get(1));

        if (value == null) {
            return encoder.encodeNil();
        }

        if (!(value instanceof RedisHash redisHash)) {
            return encoder.encodeError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        byte[] fieldValue = redisHash.get(field);
        if (fieldValue == null) {
            return encoder.encodeNil();
        }

        return encoder.encodeBulkString(fieldValue);
    }
}
