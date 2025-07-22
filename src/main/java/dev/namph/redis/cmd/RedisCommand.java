package dev.namph.redis.cmd;

import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;

import java.util.List;

public interface RedisCommand {
    void setEncoder(ProtocolEncoder encoder);
    byte[] execute(Connection connection, List<byte[]> argv);
}
