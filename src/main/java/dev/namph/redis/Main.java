package dev.namph.redis;

public class Main {
    public static void main(String[] args) {
        Integer port = null;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port 6380.");
            }
        }

        RedisServer redisServer = new RedisServer(port);
        redisServer.start();
    }
}