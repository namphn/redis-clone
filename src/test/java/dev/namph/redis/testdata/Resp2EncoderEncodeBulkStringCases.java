package dev.namph.redis.testdata;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class Resp2EncoderEncodeBulkStringCases implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
        return Stream.of(
            // case: valid bulk string
            Arguments.of(
                "Hello, World!",
                "$13Hello, World!\r\n".getBytes()
            ),
            // case: bulk string with special characters
            Arguments.of(
                "Special chars: \r\n\t",
                "$18Special chars: \r\n\t\r\n".getBytes()
            )
        );
    }
}
