package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.impl.Key;
import dev.namph.redis.store.impl.OASet;

import java.util.ArrayList;
import java.util.List;

@Cmd(name = "SRANDMEMBER", minArgs = 2)
public class SRandMember implements RedisCommand, NeedsStore {
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

        if (argv.size() > 3) {
            return encoder.encodeError("ERR syntax error");
        }

        int count = 1; // Default count
        if (argv.size() == 3) {
            try {
                count = Integer.parseInt(new String(argv.get(2)));
                if (count < 0) {
                    return encoder.encodeError("ERR count must be a non-negative integer");
                }
            } catch (NumberFormatException e) {
                return encoder.encodeError("ERR value is not an integer or out of range");
            }
        }

        if (value == null) {
            if (argv.size() == 2) return encoder.encodeNil();
            return encoder.encodeArray(List.of());
        }

        if (!(value instanceof OASet set)) {
            return encoder.encodeError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        if (count <= 1) {
            // Return a single random member
            Key randomMember = (Key) set.randomOneMember();
            if (randomMember == null) {
                return encoder.encodeNil();
            }
            return encoder.encodeBulkString(randomMember.getVal());
        }

        // Return multiple random members
        List<Key> randomMembers = set.randomMembers(count);
        if (randomMembers.isEmpty()) {
            return encoder.encodeNil();
        }
        List<byte[]> result = new ArrayList<>(randomMembers.size());
        for (Key member : randomMembers) {
            result.add(member.getVal());
        }
        return encoder.encodeArray(result);
    }
}
