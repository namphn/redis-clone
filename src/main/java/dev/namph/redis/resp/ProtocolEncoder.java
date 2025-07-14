package dev.namph.redis.resp;

import java.util.List;

public interface ProtocolEncoder {
    /**
     * Encodes an error message into the RESP protocol format.
     *
     * @param errorMessage The error message to encode.
     * @return The encoded error message as a byte array.
     */
    byte[] encodeError(String errorMessage);

    /**
     * Encodes an integer value into the RESP protocol format.
     *
     * @param value The integer value to encode.
     * @return The encoded integer as a byte array.
     */
    byte[] encodeInteger(long value);

    /**
     * Encodes a bulk string into the RESP protocol format.
     *
     * @param bulkString The bulk string to encode.
     * @return The encoded bulk string as a byte array.
     */
    byte[] encodeBulkString(String bulkString);

    /**
     * Encodes a simple string into the RESP protocol format.
     *
     * @param simpleString The simple string to encode.
     * @return The encoded simple string as a byte array.
     */
    byte[] encodeSimpleString(String simpleString);

    /**
     * Encodes list into the RESP protocol format.
     *
     * @param list The list to encode,
     * @return The encoded array as a byte array.
     */
    byte[] encodeArray(List list);
}
