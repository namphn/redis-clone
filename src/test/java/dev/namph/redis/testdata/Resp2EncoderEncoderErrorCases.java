package dev.namph.redis.testdata;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class Resp2EncoderEncoderErrorCases implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
        return Stream.of(
            // case: valid error message
            Arguments.of(
                "ERR invalid command",
                "-ERR invalid command\r\n".getBytes()
            )
        );
    }
}
