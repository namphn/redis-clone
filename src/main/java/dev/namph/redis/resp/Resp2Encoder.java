package dev.namph.redis.resp;

import dev.namph.redis.util.Resp2Syntax;
import java.util.List;

public class Resp2Encoder implements ProtocolEncoder{
    @Override
    public byte[] encodeError(String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be null or empty");
        }

        // RESP protocol requires the error message to be prefixed with '-'
        byte[] errorBytes = errorMessage.getBytes();
        return joinByte(Resp2Syntax.ERROR_PREFIX, errorBytes);
    }

    @Override
    public byte[] encodeInteger(long value) {
        // RESP protocol requires the integer to be prefixed with ':'
        String integerString = Long.toString(value);
        byte[] integerBytes = integerString.getBytes();
        return joinByte(Resp2Syntax.INTEGER_PREFIX, integerBytes);
    }

    @Override
    public byte[] encodeBulkString(String bulkString) {
        // RESP protocol requires the bulk string to be prefixed with '$'
        if (bulkString == null) {
            throw new IllegalArgumentException("Bulk string cannot be null");
        }
        byte[] bulkBytes = (bulkString.length() + bulkString).getBytes();
        return joinByte(Resp2Syntax.BULK_STRING_PREFIX, bulkBytes);
    }

    @Override
    public byte[] encodeSimpleString(String simpleString) {
        // RESP protocol requires the simple string to be prefixed with '+'
        if (simpleString == null) {
            throw new IllegalArgumentException("Simple string cannot be null");
        }
        byte[] simpleBytes = simpleString.getBytes();
        return joinByte(Resp2Syntax.SIMPLE_STRING_PREFIX, simpleBytes);
    }

    @Override
    public byte[] encodeArray(List list) {
        return new byte[0];
    }

    private static byte[] joinByte(byte prefix, byte[] buffer) {
        if (buffer == null || buffer.length == 0) {
            throw new IllegalArgumentException("Buffer cannot be null or empty");
        }
        byte[] result = new byte[buffer.length + 3];
        result[0] = prefix; // Prefix byte
        System.arraycopy(buffer, 0, result, 1, buffer.length);
        result[result.length - 2] = '\r'; // CR
        result[result.length - 1] = '\n'; // LF
        return result;
    }
}
