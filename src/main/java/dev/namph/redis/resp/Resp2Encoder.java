package dev.namph.redis.resp;

import dev.namph.redis.util.Resp2Syntax;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class Resp2Encoder implements ProtocolEncoder{
    private final Logger logger = LoggerFactory.getLogger(Resp2Encoder.class);

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
        byte[] bulkBytes = (bulkString.length() + "\r\n" + bulkString).getBytes();
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

    @Override
    public byte[] encodeNil() {
        // RESP protocol requires nil to be represented as a bulk string with length -1
        return "$-1\r\n".getBytes();
    }

    @Override
    public byte[] encodeBulkString(byte[] bulkString) {
        if (bulkString == null || bulkString.length == 0) {
            throw new IllegalArgumentException("Bulk string cannot be null or empty");
        }
        byte[] lengthBytes = (bulkString.length + "\r\n").getBytes();
        byte[] str = new byte[lengthBytes.length + bulkString.length];
        System.arraycopy(lengthBytes, 0, str, 0, lengthBytes.length);
        System.arraycopy(bulkString, 0, str, lengthBytes.length, bulkString.length);
        // RESP protocol requires the bulk string to be prefixed with '$'
        return joinByte(Resp2Syntax.BULK_STRING_PREFIX, str);
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
