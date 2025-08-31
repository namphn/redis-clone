package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.impl.RedisString;

import java.util.List;

@Cmd(name = "INCRBY", minArgs = 3)
public class IncrByCommand implements RedisCommand, NeedsStore {
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
        if (argv.size() != 3) return encoder.encodeError("ERR wrong number of arguments for 'INCRBY' command");
        long increaseVal = 0;
        try {
            increaseVal = Long.parseLong(new String(argv.get(2)));
        } catch (NumberFormatException e) {
            return encoder.encodeError("ERR wrong number of arguments for 'INCRBY' command");
        }

        var val = store.get(argv.get(1));
        if (val == null) {
            val = new RedisString(0);
        }

        if (val instanceof RedisString redisString) {
            store.remove(argv.get(1));
            long newVal = Long.parseLong(redisString.getStringValue());
            newVal += increaseVal;
            store.set(argv.get(1), new RedisString(newVal));
            return encoder.encodeInteger(newVal);
        }

        return encoder.encodeError("ERR WRONG TYPE Operation against a key holding the wrong kind of value");
    }
}
