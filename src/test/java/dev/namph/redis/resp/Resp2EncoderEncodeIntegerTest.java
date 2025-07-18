package dev.namph.redis.resp;

import dev.namph.redis.testdata.Resp2EncoderEncodeIntegerCases;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test class for encoding integers in RESP2 format.
 * This class uses parameterized tests to validate the encoding of various integer values.
 */
public class Resp2EncoderEncodeIntegerTest {
    @ParameterizedTest
    @ArgumentsSource(Resp2EncoderEncodeIntegerCases.class)
    void testEncodeInteger(int value, byte[] expectedBytes) {
        Resp2Encoder encoder = new Resp2Encoder();
        byte[] encoded = encoder.encodeInteger(value);
        if (expectedBytes != null) {
            assertArrayEquals(expectedBytes, encoded);
        } else {
            assertNull(encoded);
        }
        }
}
