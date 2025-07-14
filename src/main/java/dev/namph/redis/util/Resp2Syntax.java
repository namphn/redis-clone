package dev.namph.redis.util;

public class Resp2Syntax {
    public static final byte CR = '\r';
    public static final byte LF = '\n';
    public static final byte ERROR_PREFIX = '-';
    public static final byte SIMPLE_STRING_PREFIX = '+';
    public static final byte INTEGER_PREFIX = ':';
    public static final byte BULK_STRING_PREFIX = '$';
    public static final byte ARRAY_PREFIX = '*';
}
