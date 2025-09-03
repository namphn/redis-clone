package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.NeedsTTLStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.PersistenceStrategy;
import dev.namph.redis.store.TTLStore;
import dev.namph.redis.store.impl.Key;
import dev.namph.redis.store.impl.RdbPersistence;

import java.util.List;

@Cmd(name = "BGSAVE", minArgs = 1)
public class BgSaveCommand implements RedisCommand, NeedsStore, NeedsTTLStore<Key> {
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
        IStore cloneStore = store.clone();
        Runnable saveStore = () -> executeSaveStore(cloneStore);
        Thread thread = new Thread(saveStore);
        thread.start();
        return encoder.encodeSimpleString("Background saving started");
    }

    private static void executeSaveStore(IStore store) {
        PersistenceStrategy persistenceDB = new RdbPersistence();
        persistenceDB.save(store);
    }
}
