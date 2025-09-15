package dev.namph.redis.store.impl;

import dev.namph.redis.store.IStore;
import dev.namph.redis.store.PersistenceStrategy;
import dev.namph.redis.store.RedisValue;
import dev.namph.redis.store.TTLStore;
import dev.namph.redis.util.RdbIO;
import dev.namph.redis.util.RdbOpcode;
import dev.namph.redis.util.RdbType;
import org.slf4j.Logger;

import java.io.*;
import java.util.List;

public class RdbPersistence implements PersistenceStrategy {
    private static final String path = "./dump.rdb";
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    @Override
    @SuppressWarnings("unchecked")
    public void save(IStore store) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(path))) {
            var kvStore = (KeyValueStore.Entry[]) store.getAll();
            if (kvStore == null || kvStore.length == 0) {
                return;
            }

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
                        RdbIO.writeBytes(out, keyByte);
                        var setVal = (RedisSet) val;
                        RdbIO.writeLength(out, setVal.size());
                        for (Key k : setVal.getAll()) {
                            RdbIO.writeBytes(out, k.getVal());
                        }
                    }
                    case RedisValue.Type.LIST -> {
                        out.writeByte(RdbType.RDB_TYPE_LIST);
                        RdbIO.writeBytes(out, keyByte);
                        QuickList list = (QuickList) val;
                        RdbIO.writeLength(out, (int) list.size());
                        for (long i = 0; i < list.size(); i++) {
                            RdbIO.writeBytes(out, list.get(i));
                        }
                    }
                    case RedisValue.Type.ZSET -> {
                        out.writeByte(RdbType.RDB_TYPE_ZSET);
                        RdbIO.writeBytes(out, keyByte);
                        ZSet set = (ZSet) val;
                        RdbIO.writeLength(out, set.size());
                        for (ZSet.Entry zEntry : set.getAll()) {
                            RdbIO.writeDouble(out, zEntry.getScore());
                            RdbIO.writeBytes(out, zEntry.getKey().getVal());
                        }
                    }
                    case RedisValue.Type.HASH -> {
                        out.writeByte(RdbType.RDB_TYPE_HASH);
                        RdbIO.writeBytes(out, keyByte);
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
        store.clear();
        try (DataInputStream in = new DataInputStream(new FileInputStream(path))) {
            long expireAt = -1;
            while (in.available() > 0) {
                int type = in.readUnsignedByte();
                if (type == RdbOpcode.RDB_OPCODE_EXPIRETIME_MS) {
                    expireAt = in.readLong();
                } else {
                    byte[] key = RdbIO.readBytes(in);
                    switch (type) {
                        case RdbType.RDB_TYPE_STRING -> {
                            byte[] value = RdbIO.readBytes(in);
                            store.set(key, new RedisString(value));
                        }
                        case RdbType.RDB_TYPE_SET -> {
                            int size = (int) RdbIO.readLength(in);
                            RedisSet set = new RedisSet();
                            for (int i = 0; i < size; i++) {
                                byte[] member = RdbIO.readBytes(in);
                                set.add(new Key(member));
                            }
                            store.set(key, set);
                        }
                        case RdbType.RDB_TYPE_LIST -> {
                            int size = (int) RdbIO.readLength(in);
                            QuickList list = new QuickList();
                            for (int i = 0; i < size; i++) {
                                byte[] item = RdbIO.readBytes(in);
                                list.addLast(item);
                            }
                            store.set(key, list);
                        }
                        case RdbType.RDB_TYPE_ZSET -> {
                            int size = (int) RdbIO.readLength(in);
                            ZSet zset = new ZSet();
                            for (int i = 0; i < size; i++) {
                                double score = RdbIO.readDouble(in);
                                byte[] member = RdbIO.readBytes(in);
                                zset.add(new ZSet.Entry(new Key(member), score));
                            }
                            store.set(key, zset);
                        }
                        case RdbType.RDB_TYPE_HASH -> {
                            int size = (int) RdbIO.readLength(in);
                            RedisHash hash = new RedisHash();
                            for (int i = 0; i < size; i++) {
                                byte[] field = RdbIO.readBytes(in);
                                byte[] value = RdbIO.readBytes(in);
                                hash.add(field, value);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error when loading from file", e);
        }
    }

    @Override
    public void onCommand(List<byte[]> argv, IStore store) {

    }
}
