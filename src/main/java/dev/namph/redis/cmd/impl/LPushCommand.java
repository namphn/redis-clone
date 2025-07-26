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

@Cmd(name = "LPUSH", minArgs = 3)
public class LPushCommand implements RedisCommand, NeedsStore {
    private IStore store;
    private ProtocolEncoder encoder;

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
        var key = new Key(argv.get(1));
        var redisValues = store.get(key);
        if (redisValues == null) {
            redisValues = new QuickList();
            QuickList list = (QuickList) redisValues;
            list.setMaxChunkSize(2);
        }

        QuickList list = (QuickList) redisValues;
        long totalElement = 0;
        for (int i = 2; i < argv.size(); i++) {
            totalElement = list.addFirst(argv.get(i));
        }
        store.set(key, list);
        return encoder.encodeInteger(totalElement);
    }
}
