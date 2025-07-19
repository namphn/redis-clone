package dev.namph.redis.resp;

import dev.namph.redis.util.Resp2Syntax;
import org.slf4j.Logger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Resp2Parser implements ProtocolParser{
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Resp2Parser.class);

    @Override
    public ParseResult tryParseCommand(ByteQueue byteQueue) {
        int readAvailable = byteQueue.readAvailable();
        if (readAvailable == 0) {
            return ParseResult.IM_COMPLETE;
        }

        byteQueue.mark();
        byte firstByte = byteQueue.peek();
        if (firstByte != Resp2Syntax.ARRAY_PREFIX) {
            String errorMessage = "Protocol error: expected Array('*'), got: " + (char) firstByte;
            logger.error(errorMessage);
            byteQueue.resetToMark();
            return ParseResult.error(errorMessage);
        }

        byteQueue.readByte(); // consume the *

        byte[] lengthBytes = byteQueue.readLine();
        if (lengthBytes == null || lengthBytes.length == 0) {
            logger.info("incomplete: no length bytes found after Array('*')");
            byteQueue.resetToMark();
            return ParseResult.IM_COMPLETE;
        }

        String lengthStr = new String(lengthBytes, StandardCharsets.US_ASCII);
        int argvCount = Integer.parseInt(lengthStr);
        if (argvCount <= 0) {
            String errorMessage = "Protocol error: negative length in Array('*'): " + argvCount;
            logger.error(errorMessage);
            return ParseResult.error(errorMessage);
        }

        List<byte[]> listArgs = new ArrayList<>();
        for (int i = 0; i < argvCount; i++) {
            if (byteQueue.readAvailable() == 0) {
                logger.info("incomplete: no more data available for argument " + (i + 1));
                byteQueue.resetToMark();
                return ParseResult.IM_COMPLETE;
            }

            byte argPrefix = byteQueue.readByte();
            if (argPrefix != Resp2Syntax.BULK_STRING_PREFIX) {
                String errorMessage = "Protocol error: expected Bulk String('$'), got: " + (char) argPrefix;
                logger.error(errorMessage);
                return ParseResult.error(errorMessage);
            }
            byte[] argLengthBytes = byteQueue.readLine();
            if (argLengthBytes == null || argLengthBytes.length == 0) {
                logger.info("incomplete: no length bytes found for argument " + (i + 1));
                byteQueue.resetToMark();
                return ParseResult.IM_COMPLETE;
            }

            String argLengthStr = new String(argLengthBytes, StandardCharsets.US_ASCII);
            int argLength;
            try {
                argLength = Integer.parseInt(argLengthStr);
            } catch (NumberFormatException e) {
                String errorMessage = "Protocol error: invalid length for Bulk String: " + argLengthStr;
                logger.error(errorMessage, e);
                return ParseResult.error(errorMessage);
            }

            if (argLength < 0) {
                String errorMessage = "Protocol error: negative length in Bulk String: " + argLength;
                logger.error(errorMessage);
                return ParseResult.error(errorMessage);
            }

            byte[] argBytes = byteQueue.readBytes(argLength);
            if (argBytes == null || argBytes.length != argLength) {
                logger.info("incomplete : expected " + argLength + " bytes for argument " + (i + 1) + ", but got less");
                byteQueue.resetToMark();
                return ParseResult.IM_COMPLETE;
            }

            byte[] crlf = byteQueue.readBytes(2); // consume the CRLF after the bulk string
            if (crlf == null || crlf.length != 2) {
                logger.info("incomplete: no CRLF found after Bulk String");
                byteQueue.resetToMark();
                return ParseResult.IM_COMPLETE;
            }
            listArgs.add(argBytes);
        }

        return ParseResult.command(listArgs);
    }
}
