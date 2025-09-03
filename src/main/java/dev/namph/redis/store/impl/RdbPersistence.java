package dev.namph.redis.store.impl;

import dev.namph.redis.store.IStore;
import dev.namph.redis.store.PersistenceStrategy;
import dev.namph.redis.store.RedisValue;
import dev.namph.redis.store.TTLStore;
import dev.namph.redis.util.RdbIO;
import dev.namph.redis.util.RdbOpcode;
import dev.namph.redis.util.RdbType;
import org.slf4j.Logger;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class RdbPersistence implements PersistenceStrategy {
    private static final String path = "./dump.rdb";
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    @Override
    @SuppressWarnings("unchecked")
    public void save(IStore store) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(path))) {
            for (KeyValueStore.Entry entry : (KeyValueStore.Entry[]) store.getAll()) {
                Key key =  entry.key;
                byte[] keyByte = key.getVal();
                RedisValue val = entry.value;
                if (store.isExpired(key)) {
                    out.writeByte(RdbOpcode.RDB_OPCODE_EXPIRETIME_MS);
                    out.writeLong(store.getTTL(key));
                }

                switch (val.getType()) {
                    case RedisValue.Type.STRING -> {
                        out.writeByte(RdbType.RDB_TYPE_STRING);
                        RdbIO.writeBytes(out, keyByte);
                        RdbIO.writeBytes(out, val.getByte());
                    }
                    case RedisValue.Type.SET -> {
                        out.writeByte(RdbType.RDB_TYPE_SET);
                        var setVal = (RedisSet) val;
                        RdbIO.writeLength(out, setVal.size());
                        for (Key k : setVal.getAll()) {
                            RdbIO.writeBytes(out, k.getVal());
                        }
                    }
                    case RedisValue.Type.LIST -> {
                        out.writeByte(RdbType.RDB_TYPE_LIST);
                        QuickList list = (QuickList) val;
                        RdbIO.writeLength(out, (int) list.size());
                        for (long i = 0; i < list.size(); i++) {
                            RdbIO.writeBytes(out, list.get(i));
                        }
                    }
                    case RedisValue.Type.ZSET -> {
                        out.writeByte(RdbType.RDB_TYPE_ZSET);
                        ZSet set = (ZSet) val;
                        RdbIO.writeLength(out, set.size());
                        for (ZSet.Entry zEntry : set.getAll()) {
                            RdbIO.writeDouble(out, zEntry.getScore());
                            RdbIO.writeBytes(out, zEntry.getKey().getVal());
                        }
                    }
                    case RedisValue.Type.HASH -> {
                        out.writeByte(RdbType.RDB_TYPE_HASH);
                        RedisHash hash = (RedisHash) val;
                        RdbIO.writeLength(out, hash.size());
                        for (RedisHash.Entry hEntry : hash.getAll()) {
                            RdbIO.writeBytes(out, hEntry.getKey().getVal());
                            RdbIO.writeBytes(out, hEntry.getValue());
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error when dumping to file", e);
        }
    }

    @Override
    public void load(IStore store) {

    }

    @Override
    public void onCommand(List<byte[]> argv, IStore store) {

    }
}
