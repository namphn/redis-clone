package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.impl.Key;
import dev.namph.redis.store.impl.OASet;

import java.util.List;

@Cmd(name = "SADD", minArgs = 3)
public class SAddCommand implements RedisCommand, NeedsStore {
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
    @SuppressWarnings("unchecked")
    public byte[] execute(Connection connection, List<byte[]> argv) {
        var key = new Key(argv.get(1));
        var value = store.get(key);

        if (value != null && !(value instanceof OASet)) {
            return encoder.encodeError("WRONG TYPE Operation against a key holding the wrong kind of value");
        }

        OASet<Key> set;
        if (value == null) {
            set = new OASet<>();
            store.set(key, set);
        } else {
            set = (OASet<Key>) value;
        }

        int addedCount = 0;
        for (int i = 2; i < argv.size(); i++) {
            var member = argv.get(i);
            if (set.add(new Key(member))) {
                addedCount++;
            }
        }

        return encoder.encodeInteger(addedCount);
    }
}
