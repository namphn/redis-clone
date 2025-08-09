package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedMemoryManager;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.impl.MemoryManager;
import dev.namph.redis.store.impl.RedisHash;

import java.util.List;

@Cmd(name = "HSET", minArgs = 4)
public class HSetCommand implements RedisCommand, NeedsStore, NeedMemoryManager {
    private IStore store;
    private ProtocolEncoder encoder;
    private MemoryManager memoryManager;

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
        if (argv.size() % 2 != 0) {
            return encoder.encodeError("ERR wrong number of arguments for 'HSET' command");
        }

        var value = store.get(argv.get(1));

        if (value == null) {
            // If the key does not exist, create a new RedisHash
            value = new RedisHash();
            store.set(argv.get(1), value);
        }

        if (!(value instanceof RedisHash redisHash)) {
            return encoder.encodeError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        int count = 0;
        memoryManager.freeMemoryIfNeeded();
        for (int i = 2; i < argv.size(); i += 2) {
            redisHash.add(argv.get(i), argv.get(i + 1));
            count++;
        }

        return encoder.encodeInteger(count);
    }

    @Override
    public void setMemoryManager(MemoryManager memoryManager) {
        this.memoryManager = memoryManager;
    }
}
