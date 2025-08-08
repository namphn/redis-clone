package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.impl.Key;
import dev.namph.redis.store.impl.OASet;
import dev.namph.redis.store.impl.RedisSet;

import java.util.ArrayList;
import java.util.List;

@Cmd(name = "SMEMBERS", minArgs = 2)
public class SMembers implements RedisCommand, NeedsStore {
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
    @SuppressWarnings("unchecked")
    public byte[] execute(Connection connection, List<byte[]> argv) {
        var value = store.get(argv.get(1));

        if (value == null) {
            return encoder.encodeNil();
        }

        if (!(value instanceof RedisSet set)) {
            return encoder.encodeError("WRONG TYPE Operation against a key holding the wrong kind of value");
        }

        // Return the members of the set
        if (set.isEmpty()) {
            return encoder.encodeNil();
        }
        List<Key> keys = set.getAll();
        List<byte[]> result = new ArrayList<>(set.size());
        for (Key k : keys) {
            result.add(k.getVal());
        }
        return encoder.encodeArray(result);
    }

}
