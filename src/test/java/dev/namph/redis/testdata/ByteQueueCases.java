package dev.namph.redis.testdata;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

/**
 * Provides test cases for the ByteQueue class, specifically for reading lines from a byte queue.
 * Each case consists of input data and expected output lines.
 */
public class ByteQueueCases implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
                // case append_and_readLine
                Arguments.of(
                        "HELLO\r\nWORLD\r\n".getBytes(StandardCharsets.US_ASCII), // input data
                        new String[]{"HELLO", "WORLD"}                            // expected lines
                ),
                // case readBytes_and_crlf
                Arguments.of(
                        "012345\r\n".getBytes(StandardCharsets.US_ASCII),         // input data
                        new String[]{"012345"}                                    // expected lines (CRLF check riêng)
                )
        );
    }
}
