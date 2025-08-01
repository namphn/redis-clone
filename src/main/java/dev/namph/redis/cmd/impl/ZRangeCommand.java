package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.store.impl.Key;
import dev.namph.redis.store.impl.ZSet;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Cmd(name = "ZRANGE", minArgs = 4)
public class ZRangeCommand implements RedisCommand, NeedsStore {
    private IStore store;
    private ProtocolEncoder encoder;
    private boolean byScore;
    private boolean byLex;
    private boolean rev;
    private boolean withScores;
    private Integer limitOffset;
    private Integer limitCount;

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
        var start = 0.0;
        var end = 0.0;
        try {
            // Parse the start and end indices from the arguments
            start = Double.parseDouble(new String(argv.get(2)));
            end = Double.parseDouble(new String(argv.get(3)));
        } catch (NumberFormatException e) {
            return encoder.encodeError("ERR value is not an double or out of range");
        }

        var key = new Key(argv.get(1));
        // Retrieve the sorted set from the store
        var zSet = store.get(key);
        if (zSet == null) {
            return encoder.encodeNil();
        }

        if (!(zSet instanceof ZSet)) {
            return encoder.encodeError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        // Get the range of elements
//        var range = ((ZSet) zSet).getRange(start, end);
//        if (range.isEmpty()) {
//            return encoder.encodeNil();
//        }
//
//        try {
//            validateOptionalArgument(argv, 4);
//        } catch (IllegalArgumentException e) {
//            return encoder.encodeError(e.getMessage());
//        }
//
//        List<byte[]> result = new ArrayList<>(range.size());
//        for (ZSet.Entry entry : range) {
//            result.add(entry.getValue());
//        }

        // Encode the result as a bulk string array
        return encoder.encodeNil();
    }

    private void validateOptionalArgument(List<byte[]> argv, int index) {
        if (index < argv.size() && "BYSCORE".equalsIgnoreCase(new String(argv.get(index)))) {
            byScore = true;
            index++;
        } else if (index < argv.size() && "BYLEX".equalsIgnoreCase(new String(argv.get(index)))) {
            byLex = true;
            index++;
        }

        // [REV]
        if (index < argv.size() && "REV".equalsIgnoreCase(new String(argv.get(index)))) {
            rev = true;
            index++;
        }

        // [LIMIT offset count]
        if (index < argv.size() && "LIMIT".equalsIgnoreCase(new String(argv.get(index)))) {
            if (index + 2 >= argv.size()) {
                throw new IllegalArgumentException("LIMIT requires offset and count");
            }
            try {
                limitOffset = Integer.parseInt(new String(argv.get(index + 1)));
                limitCount = Integer.parseInt(new String(argv.get(index + 2)));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("LIMIT offset and count must be integers");
            }
            index += 3;
        }

        // [WITHSCORES]
        if (index < argv.size() && "WITHSCORES".equalsIgnoreCase(new String(argv.get(index)))) {
            withScores = true;
            index++;
        }

        if (index < argv.size()) {
            throw new IllegalArgumentException("ERR wrong number of arguments for 'ZRANGE' command");
        }
    }
}
