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

@Cmd( name = "LPOP", minArgs = 2)
public class LPopCommand implements RedisCommand, NeedsStore {
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
        if (argv.size() > 3) {
            return encoder.encodeError("ERR syntax error");
        }

        var key = new Key(argv.get(1));
        var redisValues = store.get(key);

        if (redisValues == null) {
            return encoder.encodeNil();
        }

        if (!(redisValues instanceof QuickList list)) {
            return encoder.encodeError("ERR value is not a list");
        }

        if (list.isEmpty()) {
            return encoder.encodeNil();
        }

        if (argv.size() == 2) {
            byte[] value = list.removeFirst();
            store.set(key, list);
            return encoder.encodeBulkString(value);
        }

        long count;
        try {
            count = Long.parseLong(new String(argv.get(2)));
        } catch (NumberFormatException e) {
            return encoder.encodeError("ERR value is not an integer or out of range");
        }

        if (count <= 0) {
            return encoder.encodeNil();
        }

        List<byte[]> values = list.removeFirst(count);
        store.set(key, list);
        if (values == null || values.isEmpty()) {
            return encoder.encodeNil();
        }
        return encoder.encodeArray(values);
    }
}
