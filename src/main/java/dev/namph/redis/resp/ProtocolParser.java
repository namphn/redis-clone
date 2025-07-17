package dev.namph.redis.resp;

import java.util.List;

public interface ProtocolParser {
    enum Type {
        IM_COMPLETE,
        ERROR,
        COMMAND,
    }

    record ParseResult(Type type, String message, List<byte[]> args) {
        public static final ParseResult IM_COMPLETE = new ParseResult(Type.IM_COMPLETE, null, null);
        public static ParseResult error(String message) {
            return new ParseResult(Type.ERROR, message, null);
        }
        public static ParseResult command(List<byte[]> args) {
            return new ParseResult(Type.COMMAND, null, args);
        }
    }

    ParseResult tryParseCommand(ByteQueue byteQueue);
}
