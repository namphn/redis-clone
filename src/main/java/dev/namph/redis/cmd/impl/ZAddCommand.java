package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.impl.Key;
import dev.namph.redis.store.impl.ZSet;

import java.util.List;

@Cmd(name = "ZADD", minArgs = 4)
public class ZAddCommand implements NeedsStore, RedisCommand {
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
        if (argv.size() % 2 != 0) {
            return encoder.encodeError("ERR wrong number of arguments for 'ZADD' command");
        }
        int countAdd = 0;
        var zSet = store.get(new Key(argv.get(1)));
        if (zSet == null) {
            // If the key does not exist, create a new ZSet
            zSet = new ZSet();
            store.set(new Key(argv.get(1)), zSet);
        } else if (!(zSet instanceof ZSet)) {
            return encoder.encodeError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        for (int i = 2; i < argv.size(); i += 2) {
            double score;
            try {
                score = Double.parseDouble(new String(argv.get(i)));
            } catch (NumberFormatException e) {
                return encoder.encodeError("ERR value is not a valid float");
            }
            ZSet zSetInstance = (ZSet) zSet;
            var entry = new ZSet.Entry(argv.get(i + 1), score);
            // O(1) check if the entry already exists
            if (!zSetInstance.contains(entry)) {
                countAdd++;
            }
            zSetInstance.remove(entry);
            zSetInstance.add(entry);
        }

        // Return the number of elements added to the sorted set
        return encoder.encodeInteger(countAdd);
    }
}
