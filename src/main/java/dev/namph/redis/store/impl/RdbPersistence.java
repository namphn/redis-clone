package dev.namph.redis.store.impl;

import dev.namph.redis.store.IStore;
import dev.namph.redis.store.PersistenceStrategy;
import dev.namph.redis.store.RedisValue;
import dev.namph.redis.store.TTLStore;
import dev.namph.redis.util.RdbIO;
import dev.namph.redis.util.RdbOpcode;
import dev.namph.redis.util.RdbType;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class RdbPersistence implements PersistenceStrategy {
    private static final String path = "./dump.rdb";

    @Override
    @SuppressWarnings("unchecked")
    public void save(IStore store, TTLStore ttlStore) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(path))) {
            for (KeyValueStore.Entry entry : (KeyValueStore.Entry[]) store.getAll()) {
                Key key =  entry.key;
                byte[] keyByte = key.getVal();
                RedisValue val = entry.value;
                if (ttlStore.isExpired(key)) {
                    out.writeByte(RdbOpcode.RDB_OPCODE_EXPIRETIME_MS);
                    out.writeLong(ttlStore.getTTL(key));
                }

                switch (val.getType()) {
                    case RedisValue.Type.STRING:
                        out.writeByte(RdbType.RDB_TYPE_STRING);
                        break;
                    case RedisValue.Type.SET:
                        out.writeByte(RdbType.RDB_TYPE_SET);
                        break;
                    case RedisValue.Type.LIST:
                        out.writeByte(RdbType.RDB_TYPE_LIST);
                        break;
                    case RedisValue.Type.ZSET:
                        out.writeByte(RdbType.RDB_TYPE_ZSET);
                        break;
                    case RedisValue.Type.HASH:
                        out.writeByte(RdbType.RDB_TYPE_HASH);
                        break;
                }

                out.write(keyByte);

            }
        } catch (IOException e) {

        }
    }

    @Override
    public void load(IStore store) {

    }

    @Override
    public void onCommand(List<byte[]> argv, IStore store) {

    }
}
