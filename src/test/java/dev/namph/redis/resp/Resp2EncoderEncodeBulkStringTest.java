package dev.namph.redis.resp;

import dev.namph.redis.testdata.Resp2EncoderEncodeBulkStringCases;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Resp2EncoderEncodeBulkStringTest {
    @ParameterizedTest
    @ArgumentsSource(Resp2EncoderEncodeBulkStringCases.class)
    void testEncodeBulkString(String value, byte[] expectedBytes) {
        Resp2Encoder encoder = new Resp2Encoder();
        byte[] encoded = encoder.encodeBulkString(value);
        if (expectedBytes != null) {
            assertArrayEquals(expectedBytes, encoded);
        } else {
            assertNull(encoded);
        }
    }
}
