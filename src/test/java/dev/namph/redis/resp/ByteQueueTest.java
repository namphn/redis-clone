package dev.namph.redis.resp;

import dev.namph.redis.testdata.ByteQueueCases;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ByteQueueTest {
    @ParameterizedTest(name = "{index} => input={0}, expectedLines={1}")
    @ArgumentsSource(ByteQueueCases.class)
    void testReadLine(byte[] input, String[] expectedLines) {
        ByteQueue q = new ByteQueue(8);
        q.append(ByteBuffer.wrap(input));

        for (String expected : expectedLines) {
            byte[] line = q.readLine();
            assertNotNull(line);
            assertEquals(expected, new String(line, StandardCharsets.US_ASCII));
        }
    }
}
