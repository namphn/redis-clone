package dev.namph.redis.cmd;

import dev.namph.redis.net.Connection;

import java.util.List;

public interface RedisCommand {
    byte[] execute(Connection connection, List<byte[]> argv);
}
