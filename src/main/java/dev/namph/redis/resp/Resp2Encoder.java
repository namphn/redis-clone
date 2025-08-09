package dev.namph.redis.resp;

import dev.namph.redis.util.Resp2Syntax;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Resp2Encoder implements ProtocolEncoder{
    private static final byte[] CRLF = new byte[] {'\r', '\n'};
    private static final byte[] MINUS_ONE_CRLF = new byte[] {'-', '1', '\r', '\n'};


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
    public byte[] encodeArray(List<byte[]> list) {
        // todo: use ByteBuffer instead of ByteArrayOutputStream for better performance
        // Null array
        if (list == null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(5);
            out.write(Resp2Syntax.ARRAY_PREFIX); // '*'
            writeMinusOneCRLF(out);
            return out.toByteArray();
        }

        // Empty array
        if (list.isEmpty()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(5);
            out.write(Resp2Syntax.ARRAY_PREFIX); // '*'
            writeInt(out, 0);
            writeCRLF(out);
            return out.toByteArray();
        }

        int estimated = estimateArraySize(list);
        ByteArrayOutputStream out = new ByteArrayOutputStream(estimated);

        // *<n>\r\n
        out.write(Resp2Syntax.ARRAY_PREFIX); // '*'
        writeInt(out, list.size());
        writeCRLF(out);

        for (byte[] item : list) {
            out.write(Resp2Syntax.BULK_STRING_PREFIX); // '$'
            if (item == null) {
                // $-1\r\n
                writeMinusOneCRLF(out);
            } else {
                // $<len>\r\n<bytes>\r\n
                writeInt(out, item.length);
                writeCRLF(out);
                out.write(item, 0, item.length);
                writeCRLF(out);
            }
        }

        return out.toByteArray();
    }

    private static int estimateArraySize(List<byte[]> list) {
        // "*<n>\r\n"
        int n = list.size();
        int total = 1 + digits(n) + 2;

        for (byte[] item : list) {
            if (item == null) {
                // "$-1\r\n"
                total += 1 + 4;
            } else {
                // "$" + "<len>" + "\r\n" + "<bytes>" + "\r\n"
                int len = item.length;
                total += 1 + digits(len) + 2 + len + 2;
            }
        }
        return total;
    }

    private static int digits(int v) {
        // Số chữ số thập phân của v (v >= 0)
        if (v < 10) return 1;
        if (v < 100) return 2;
        if (v < 1000) return 3;
        if (v < 10000) return 4;
        if (v < 100000) return 5;
        if (v < 1000000) return 6;
        if (v < 10000000) return 7;
        if (v < 100000000) return 8;
        if (v < 1000000000) return 9;
        return 10;
    }

    private static void writeInt(ByteArrayOutputStream out, int value) {
        byte[] b = Integer.toString(value).getBytes(StandardCharsets.UTF_8);
        out.write(b, 0, b.length);
    }

    private static void writeCRLF(ByteArrayOutputStream out) {
        out.write(CRLF, 0, CRLF.length);
    }

    private void writeMinusOneCRLF(ByteArrayOutputStream out) {
        out.write(MINUS_ONE_CRLF, 0, MINUS_ONE_CRLF.length);
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
