package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.*;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.TTLStore;
import dev.namph.redis.store.impl.Key;
import dev.namph.redis.store.impl.MemoryManager;
import dev.namph.redis.store.impl.RedisString;

import java.util.List;

/**
 * The SET command is used to set the value of a key in the Redis store.
 * It takes at least three arguments: the command name, the key, and the value.
 * If successful, it returns "OK".
 */
@Cmd(name = "SET", minArgs = 3)
public class SetCommand implements RedisCommand, NeedsStore, NeedMemoryManager, NeedsTTLStore<Key> {
    private IStore store;
    private TTLStore<Key> ttlStore;
    private ProtocolEncoder encoder;
    private MemoryManager memoryManager;
    private boolean xx;
    private boolean nx;
    private int ttlSecond;
    private int ttlMilliSecond;
    private long ttlUnixTimeSecond;
    private long ttlUnixTimeMilliSecond;
    private boolean keepTtl;

    @Override
    public void setEncoder(ProtocolEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public byte[] execute(Connection connection, List<byte[]> argv) {
        memoryManager.freeMemoryIfNeeded();
        resetOptional();
        try {
            extractOptional(argv, 3);
        } catch (IllegalArgumentException e) {
            encoder.encodeError(e.getMessage());
        }

        // Store the key-value pair in the store
        setKey(argv);

        // Return a simple string response indicating success
        return encoder.encodeSimpleString("OK");
    }

    private void setKey(List<byte[]> argv) {
        // only set if key does not exist
        if (nx && store.contains(argv.get(1))) return;
        // only set if key exists
        if (xx && !store.contains(argv.get(1))) return;

        store.remove(argv.get(1));
        store.set(argv.get(1), new RedisString(argv.get(2)));
        // keep time to live
        if (keepTtl) return;

        Key key = new Key(argv.get(1));
        if (ttlSecond > 0) {
            long expireAt = System.currentTimeMillis() + (ttlSecond * 1000L);
            ttlStore.setTTL(key, expireAt);
        } else if (ttlMilliSecond > 0) {
            long expireAt = System.currentTimeMillis() + ttlMilliSecond;
            ttlStore.setTTL(key, expireAt);
        } else if (ttlUnixTimeSecond > 0) {
            long expireAt = ttlUnixTimeSecond * 1000L;
            ttlStore.setTTL(key, expireAt);
        } else if (ttlUnixTimeMilliSecond > 0) {
            ttlStore.setTTL(key, ttlUnixTimeMilliSecond);
        } else {
            ttlStore.removeTTL(key);
        }
    }

    @Override
    public void setStore(IStore store) {
        this.store = store;
    }

    @Override
    public void setMemoryManager(MemoryManager memoryManager) {
        this.memoryManager = memoryManager;
    }

    private void extractOptional(List<byte[]> argv, int fromIndex) {
        if (fromIndex >= argv.size()) return;

        if ("XX".equalsIgnoreCase(new String(argv.get(fromIndex)))) {
            xx = true;
            fromIndex++;
        } else if ("NX".equalsIgnoreCase(new String(argv.get(fromIndex)))) {
            nx = true;
            fromIndex++;
        }

        if (fromIndex >= argv.size()) return;

        if ("EX".equalsIgnoreCase(new String(argv.get(fromIndex)))) {
            fromIndex++;
            if (fromIndex >= argv.size()) throw new IllegalArgumentException("ERR syntax error");

            try {
                ttlSecond = Integer.parseInt(new String(argv.get(fromIndex)));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ERR invalid expire time in set");
            }
        } else if ("PX".equalsIgnoreCase(new String(argv.get(fromIndex)))) {
            fromIndex++;
            if (fromIndex >= argv.size()) throw new IllegalArgumentException("ERR syntax error");

            try {
                ttlMilliSecond = Integer.parseInt(new String(argv.get(fromIndex)));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ERR invalid expire time in set");
            }
        } else if ("EXAT".equalsIgnoreCase(new String(argv.get(fromIndex)))) {
            fromIndex++;
            if (fromIndex >= argv.size()) throw new IllegalArgumentException("ERR syntax error");

            try {
                ttlUnixTimeSecond = Long.parseLong(new String(argv.get(fromIndex)));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ERR invalid expire time in set");
            }
        } else if ("PXAT".equalsIgnoreCase(new String(argv.get(fromIndex)))) {
            fromIndex++;
            if (fromIndex >= argv.size()) throw new IllegalArgumentException("ERR syntax error");

            try {
                ttlUnixTimeMilliSecond = Long.parseLong(new String(argv.get(fromIndex)));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ERR invalid expire time in set");
            }
        }else if ("KEEPTTL".equalsIgnoreCase(new String(argv.get(fromIndex)))) {
            keepTtl = true;
        }
    }

    private void resetOptional() {
        this.xx = false;
        this.nx = false;
        this.ttlSecond = 0;
        this.ttlMilliSecond = 0;
        this.ttlUnixTimeSecond = 0;
        this.ttlUnixTimeMilliSecond = 0;
        this.keepTtl = false;
    }

    @Override
    public void setTTLStore(TTLStore<Key> store) {
        this.ttlStore = store;
    }
}
