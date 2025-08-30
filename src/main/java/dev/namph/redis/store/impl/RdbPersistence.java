package dev.namph.redis.store.impl;

import dev.namph.redis.store.IStore;
import dev.namph.redis.store.PersistenceStrategy;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class RdbPersistence implements PersistenceStrategy {
    private static final String path = "./dump.rdb";

    @Override
    public void save(IStore store) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(path))) {

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
