package dev.namph.redis.testdata;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

/**
 * Provides test cases for encoding integers in RESP2 format.
 * Each case consists of an integer and its expected byte representation.
 */
public class Resp2EncoderEncodeIntegerCases implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
        return Stream.of(
            // case: valid integer
            Arguments.of(
                42,
                ":42\r\n".getBytes()
            ),
            // case: negative integer
            Arguments.of(
                -100,
                ":-100\r\n".getBytes()
            ),
            // case: zero
            Arguments.of(
                0,
                ":0\r\n".getBytes()
            )
        );
    }
}
