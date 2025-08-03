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
    private int limitOffset;
    private int limitCount;
    private int start;
    private int end;
    private double startScore;
    private double endScore;
    private byte[] startLex;
    private byte[] endLex;
    private boolean includeStartLex;
    private boolean includeEndLex;

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


        var key = new Key(argv.get(1));
        // Retrieve the sorted set from the store
        var zSet = store.get(key);
        if (zSet == null) {
            return encoder.encodeNil();
        }

        if (!(zSet instanceof ZSet)) {
            return encoder.encodeError("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        List<byte[]> result;
        // Reset optional arguments before processing
        resetOptionalArgument();
        try {
            validateOptionalArgument(argv);
            result = fetchZSet((ZSet) zSet);
        } catch (IllegalArgumentException e) {
            return encoder.encodeError(e.getMessage());
        }

        if (result.isEmpty()) {
            return encoder.encodeNil();
        }
        return encoder.encodeArray(result);
    }

    private void validateOptionalArgument(List<byte[]> argv) throws IllegalArgumentException {
        int index = 4;
        if (index < argv.size() && "BYSCORE".equalsIgnoreCase(new String(argv.get(index)))) {
            byScore = true;
            try {
                startScore = Double.parseDouble(new String(argv.get(2)));
                endScore = Double.parseDouble(new String(argv.get(3)));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ERR min or max not valid float range item");
            }
            index++;
        } else if (index < argv.size() && "BYLEX".equalsIgnoreCase(new String(argv.get(index)))) {
            byLex = true;
            index++;
            var startLexStr = new String(argv.get(2));
            var endLexStr = new String(argv.get(3));

            if (startLexStr.isEmpty() || endLexStr.isEmpty()) {
                throw new IllegalArgumentException("ERR min or max not valid string range item");
            }

            var startLexOption = startLexStr.charAt(1);
            var endLexOption = endLexStr.charAt(1);

            if (startLexOption != '[' && startLexOption != '(' && startLexOption != '-') {
                throw new IllegalArgumentException("ERR min or max not valid string range item");
            }
            if (endLexOption != '[' && endLexOption != '(' && endLexOption != '+') {
                throw new IllegalArgumentException("ERR min or max not valid string range item");
            }

            includeStartLex = startLexOption == '[';
            includeEndLex = endLexOption == '[';

            System.arraycopy(argv.get(2), 1, this.startLex, 0, argv.get(2).length - 1);
            System.arraycopy(argv.get(3), 1, this.endLex, 0, argv.get(3).length - 1);
        } else {
            // Default case:
            try {
                start = Integer.parseInt(new String(argv.get(2)));
                end = Integer.parseInt(new String(argv.get(3)));
                if (start < 0 || end < 0 || start > end) {
                    throw new IllegalArgumentException("ERR min or max not valid integer range item");
                }
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("ERR min or max not valid integer range item");
            }
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

    private List<byte[]> fetchZSet(ZSet zSet) throws IllegalArgumentException {
        List<byte[]> result = new ArrayList<>();
        if (byScore) {
            if (rev) {
                // Get range by score in reverse order
                var range = zSet.getRangeByScoreReversed(startScore, endScore, limitCount, limitOffset);
                for (ZSet.Entry entry : range) {
                    result.add(entry.getKey().getVal());
                    if (withScores) {
                        result.add(String.valueOf(entry.getScore()).getBytes(StandardCharsets.UTF_8));
                    }
                }
                return result;
            } else {
                // Get range by score
                var range = zSet.getRangeByScore(startScore, endScore, limitCount, limitOffset);
                for (ZSet.Entry entry : range) {
                    result.add(entry.getKey().getVal());
                    if (withScores) {
                        result.add(String.valueOf(entry.getScore()).getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        } else if (byLex) {
            if (rev) {
                // Get range by lexicographical order in reverse
                var range = zSet.getRangeByLexReversed(startLex, endLex, limitCount, limitOffset, includeStartLex, includeEndLex);
                for (ZSet.Entry entry : range) {
                    result.add(entry.getKey().getVal());
                    if (withScores) {
                        result.add(String.valueOf(entry.getScore()).getBytes(StandardCharsets.UTF_8));
                    }
                }
            } else {
                // Get range by lexicographical order
                var range = zSet.getRangeByLex(startLex, endLex, limitCount, limitOffset, includeStartLex, includeEndLex);
                for (ZSet.Entry entry : range) {
                    result.add(entry.getKey().getVal());
                    if (withScores) {
                        result.add(String.valueOf(entry.getScore()).getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        } else {
            // Default range
            if (rev) {
                // Get range by rank in reverse order
                var range = zSet.getRangeByRankReversed(start, end, limitCount, limitOffset);
                for (ZSet.Entry entry : range) {
                    result.add(entry.getKey().getVal());
                    if (withScores) {
                        result.add(String.valueOf(entry.getScore()).getBytes(StandardCharsets.UTF_8));
                    }
                }
            } else {
                var range = zSet.getRangeByRank(start, end, limitCount, limitOffset);
                for (ZSet.Entry entry : range) {
                    result.add(entry.getKey().getVal());
                    if (withScores) {
                        result.add(String.valueOf(entry.getScore()).getBytes(StandardCharsets.UTF_8));
                    }
                }
            }

        }
        return result;
    }

    private void resetOptionalArgument() {
        byScore = false;
        byLex = false;
        rev = false;
        withScores = false;
        limitOffset = 0;
        limitCount = 0;
        start = 0;
        end = 0;
        startScore = 0.0;
        endScore = 0.0;
        startLex = new byte[0];
        endLex = new byte[0];
        includeStartLex = false;
        includeEndLex = false;
    }
}
