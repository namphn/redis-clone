package dev.namph.redis.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class RdbIO {
    public static void writeLength(DataOutputStream out, int len) throws IOException {
        // if len < 64
        if (len < (1<<6)) {
            // mark 1 byte
            out.writeByte(len & 0x3F);
        } else if (len < (1 << 14)) {
            // mark 2 byte
            out.writeByte((len >> 8) & 0x3F | 0x40);
            out.writeByte(len & 0xFF);
        } else {
            out.writeByte(0x80);
            out.writeByte(len);
        }
    }

    public static int readLength(DataInputStream in) throws IOException {
        int first = in.readUnsignedByte();
        int type = (first & 0xC0) >> 6;
        if (type == 0) {
            return first & 0x3F;
        } else if (type == 1) {
            int second = in.readUnsignedByte();
            return ((first & 0x3F) << 8) | second;
        } else if (type == 2) {
            return in.readUnsignedByte();
        } else {
            throw new IOException("Special encoding not implemented yet");
        }
    }

    public static void writeString(DataOutputStream out, String s) throws IOException {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        writeLength(out, b.length);
        out.write(b);
    }

    public static void writeDouble(DataOutputStream out, double d) throws IOException {
        String s = String.format(Locale.US, "%.17g", d);
        writeString(out, s);
    }

    public static void writeBytes(DataOutputStream out, byte[] b) throws IOException {
        writeLength(out, b.length);
        out.write(b);
    }

    public static byte[] readBytes(InputStream in) throws IOException {
        int len = readLength(new DataInputStream(in));
        byte[] b = new byte[len];
        int read = 0;
        while (read < len) {
            int n = in.read(b, read, len - read);
            if (n == -1) {
                throw new IOException("Unexpected end of stream");
            }
            read += n;
        }
        return b;
    }

    public static double readDouble(DataInputStream in) throws IOException {
        String s = readString(in);
        return Double.parseDouble(s);
    }

    public static String readString(DataInputStream in) throws IOException {
        int len = readLength(in);
        byte[] b = new byte[len];
        in.readFully(b);
        return new String(b, StandardCharsets.UTF_8);
    }
}
