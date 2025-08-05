package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.NeedsTTLStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.TTLStore;
import dev.namph.redis.store.impl.Key;

import java.util.List;

@Cmd(name = "EXPIRE", minArgs = 3)
public class ExpireCommand implements RedisCommand, NeedsStore, NeedsTTLStore<Key> {
    private IStore store;
    private TTLStore<Key> ttlStore;
    private ProtocolEncoder encoder;

    @Override
    public void setStore(IStore store) {
        this.store = store;
    }

    @Override
    public void setTTLStore(TTLStore<Key> store) {
        this.ttlStore = store;
    }

    @Override
    public void setEncoder(ProtocolEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public byte[] execute(Connection connection, List<byte[]> argv) {
        var key = new Key(argv.get(1));
        var value = store.get(key);
        if (value == null) {
            return encoder.encodeInteger(0);
        }
        long seconds;
        try {
            seconds = Long.parseLong(new String(argv.get(2)));
            if (seconds < 0) {
                return encoder.encodeError("ERR invalid expire time in EXPIRE");
            }
        } catch (NumberFormatException e) {
            return encoder.encodeError("ERR value is not an integer or out of range");
        }
        long expireAt = System.currentTimeMillis() + (seconds * 1000);
        ttlStore.setTTL(key, expireAt);
        return encoder.encodeInteger(1);
    }
}
