package dev.namph.redis.util;

import dev.namph.redis.resp.ProtocolEncoder;
import dev.namph.redis.resp.Resp2Encoder;

public class Singleton {
    private Singleton() {
        // Private constructor to prevent instantiation
    }

    private static class Holder {
        private static final ProtocolEncoder RESP2_ENCODER_INSTANCE = new Resp2Encoder();
    }

    public static ProtocolEncoder getResp2Encoder() {
        return Holder.RESP2_ENCODER_INSTANCE;
    }
}
