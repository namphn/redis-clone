package dev.namph.redis.cmd.impl;

import dev.namph.redis.cmd.Cmd;
import dev.namph.redis.cmd.NeedsStore;
import dev.namph.redis.cmd.RedisCommand;
import dev.namph.redis.net.Connection;
import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.store.IStore;
import dev.namph.redis.util.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class CommandRegistry {
    private IStore store;
    private final Map<String, RedisCommand> commands = new HashMap<>();
    private final Map<String, Cmd> commandMetadata = new HashMap<>();
    private final ProtocolEncoder encoder = Singleton.getResp2Encoder();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public CommandRegistry(IStore store) {
        this.store = store;

        ServiceLoader<RedisCommand> loader = ServiceLoader.load(RedisCommand.class);
        for (RedisCommand command : loader) {
            Cmd cmdAnnotation = command.getClass().getAnnotation(Cmd.class);
            if (cmdAnnotation == null) {
                throw new IllegalStateException("Command class " + command.getClass().getName() + " is missing @Cmd annotation");
            }

            String name = cmdAnnotation.name().toUpperCase(Locale.ROOT);
            if (commands.containsKey(name)) {
                throw new IllegalStateException("Duplicate command name: " + name);
            }
            if (command instanceof NeedsStore ns) ns.setStore(store);

            commands.put(name, command);
            commandMetadata.put(name, cmdAnnotation);
        }
    }

    public byte[] dispatch(Connection connection, List<byte[]> argv) {
        if (argv == null || argv.isEmpty()) {
            return encoder.encodeError("ERR invalid command");
        }
        String commandName = new String(argv.get(0), StandardCharsets.US_ASCII).toUpperCase(Locale.ROOT);
        var command = commands.get(commandName);

        String errorMessage;
        if (command == null) {
            errorMessage = "ERR unknown command '" + commandName + "'";
            logger.warn(errorMessage);
            return encoder.encodeError(errorMessage);
        }

        Cmd cmdAnnotation = commandMetadata.get(commandName);
        if (argv.size() < cmdAnnotation.minArgs()) {
            errorMessage = "ERR wrong number of arguments for '" + commandName + "' command";
            logger.warn(errorMessage);
            return encoder.encodeError(errorMessage);
        }

        return command.execute(connection, argv);
    }
}
