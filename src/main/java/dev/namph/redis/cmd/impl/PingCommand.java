package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.util.Singleton;

import java.util.List;

/**
 * The PING command is used to test the connection to the Redis server.
 * It can also be used to check if the server is running.
 * If an argument is provided, it will return that argument as a simple string.
 * Otherwise, it returns "PONG".
 */
@Cmd(name = "PING", minArgs = 1)
public class PingCommand implements RedisCommand {
    private ProtocolEncoder encoder = Singleton.getResp2Encoder();

    @Override
    public void setEncoder(ProtocolEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public byte[] execute(Connection connection, List<byte[]> argv) {
        if (argv.size() > 1) return encoder.encodeSimpleString(new String(argv.get(1)));
        return encoder.encodeSimpleString("PONG");
    }
}
