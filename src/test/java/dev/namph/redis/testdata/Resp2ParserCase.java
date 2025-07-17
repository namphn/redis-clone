package dev.namph.redis.testdata;

import dev.namph.redis.resp.ByteQueue;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Stream;

public class Resp2ParserCase implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        ByteQueue queue = new ByteQueue(0);
        queue.append(ByteBuffer.wrap("SET key value\r\n".getBytes()));

        ByteQueue queue2 = new ByteQueue(0);
        queue2.append(ByteBuffer.wrap("\r\n".getBytes()));

        ByteQueue queue3 = new ByteQueue(0);
        queue3.append(ByteBuffer.wrap("*3\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n".getBytes()));

        return Stream.of(
            // case: valid command with arguments
            Arguments.of(
                    queue,
                null,
                "Protocol error: expected Array('*'), got: S"
            ),
            // case: empty command
            Arguments.of(
                    queue2,
                    null,
                "Protocol error: expected Array('*'), got: \r"
            ),
            // case: command with multiple arguments
            Arguments.of(
                    queue3,
                    List.of("SET".getBytes(), "key".getBytes(), "value".getBytes()),
                null
            )
        );
    }
}
