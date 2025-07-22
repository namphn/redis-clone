package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;

import java.util.List;

/**
 * The SET command is used to set the value of a key in the Redis store.
 * It takes at least three arguments: the command name, the key, and the value.
 * If successful, it returns "OK".
 */
@Cmd(name = "SET", minArgs = 3)
public class SetCommand implements RedisCommand, NeedsStore {
    private IStore store;
    private ProtocolEncoder encoder;

    @Override
    public void setEncoder(ProtocolEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public byte[] execute(Connection connection, List<byte[]> argv) {
        if (argv.size() < 3) {
            return "ERR wrong number of arguments for 'set' command".getBytes();
        }

        byte[] key = argv.get(1);
        byte[] value = argv.get(2);

        if (argv.size() > 3) {
            // todo handle additional options like EX, PX, NX, XX
            if (!validateOptions(argv.subList(3, argv.size()))) {
                return encoder.encodeError("ERR invalid options for 'set' command");
            }
        }
        // Store the key-value pair in the store
        store.set(key, value);

        // Return a simple string response indicating success
        return encoder.encodeSimpleString("OK");
    }

    @Override
    public void setStore(IStore store) {
        this.store = store;
    }

    private boolean validateOptions(List<byte[]> argv) {
        // Validate options like EX, PX, NX, XX if provided
        // This is a placeholder for future implementation
        return false;
    }
}
