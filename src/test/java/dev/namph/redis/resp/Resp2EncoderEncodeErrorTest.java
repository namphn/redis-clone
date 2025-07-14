package dev.namph.redis.resp;

import dev.namph.redis.testdata.Resp2EncoderEncoderErrorCases;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Resp2EncoderEncodeErrorTest {
    @ParameterizedTest
    @ArgumentsSource(Resp2EncoderEncoderErrorCases.class)
    void testEncodeError(String errorMessage, byte[] expectedBytes) {
        Resp2Encoder encoder = new Resp2Encoder();
        byte[] encoded = encoder.encodeError(errorMessage);
        if (expectedBytes != null) {
            assertArrayEquals(expectedBytes, encoded);
        } else {
            assertNull(encoded);
        }

    }
}
