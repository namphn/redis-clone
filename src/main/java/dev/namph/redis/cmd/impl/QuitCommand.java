package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;

import java.util.List;

/**
 * The QUIT command is used to close the connection to the Redis server.
 * It does not require any arguments and returns an empty response.
 */
@Cmd(name = "QUIT", minArgs = 1)
public class QuitCommand implements RedisCommand {
    @Override
    public byte[] execute(Connection connection, List<byte[]> argv) {
        // Close the connection and return an empty byte array
        connection.quit();
        return new byte[0]; // RESP2 does not require a response for QUIT
    }
}
