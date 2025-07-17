package dev.namph.redis.resp;

import dev.namph.redis.testdata.Resp2ParserCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Resp2ParserTest {
    @ParameterizedTest
    @ArgumentsSource(Resp2ParserCase.class)
    void testTryParseCommand(ByteQueue queue, List<byte[]> expectedCommand, String expectedMessage) {
        ProtocolParser parser = new Resp2Parser();
        ProtocolParser.ParseResult result = parser.tryParseCommand(queue);
        assertNotNull(result);
        if (expectedCommand != null) {
            assertNotNull(result.args());
            assertEquals(expectedCommand.size(), result.args().size());
            for (int i = 0; i < expectedCommand.size(); i++) {
                assertEquals(new String(expectedCommand.get(i)), new String(result.args().get(i)));
            }
        } else {
            assertEquals(expectedMessage, result.message());
        }
    }
}
