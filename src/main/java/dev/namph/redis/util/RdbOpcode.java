package dev.namph.redis.util;

public class RdbOpcode {
    public static final int RDB_OPCODE_EXPIRETIME = 0xFC;     // 4 byte seconds
    public static final int RDB_OPCODE_EXPIRETIME_MS = 0xFD;  // 8 byte millis
    public static final int RDB_OPCODE_SELECTDB = 0xFE;
    public static final int RDB_OPCODE_EOF = 0xFF;
}
