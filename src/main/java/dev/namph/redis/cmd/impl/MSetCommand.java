package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedMemoryManager;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.impl.MemoryManager;
import dev.namph.redis.store.impl.RedisString;

import java.util.List;

@Cmd(name = "MSET", minArgs = 3)
public class MSetCommand implements RedisCommand, NeedsStore, NeedMemoryManager {
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
        if (argv.size() % 2 != 1) {
            return encoder.encodeError("ERR wrong number of arguments for 'mset' command");
        }

        memoryManager.freeMemoryIfNeeded();
        for (int i = 1; i < argv.size(); i += 2) {
            var value = new RedisString(argv.get(i + 1));
            store.set(argv.get(i), value);
        }

        return encoder.encodeSimpleString("OK");
    }

    @Override
    public void setMemoryManager(MemoryManager memoryManager) {
        this.memoryManager = memoryManager;
    }
}
