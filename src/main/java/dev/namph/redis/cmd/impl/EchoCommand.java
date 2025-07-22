package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.util.Singleton;

import java.util.List;

/**
 * The ECHO command is used to return the input string to the client.
 */
@Cmd(name = "ECHO", minArgs = 2)
public class EchoCommand implements RedisCommand {
    private ProtocolEncoder encoder;

    @Override
    public void setEncoder(ProtocolEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public byte[] execute(Connection connection, List<byte[]> argv) {
        if (argv.size() < 2) {
            return "ERR wrong number of arguments for 'echo' command".getBytes();
        }
        // Return the first argument as a simple string
        return encoder.encodeSimpleString(new String(argv.get(1)));
    }
}
