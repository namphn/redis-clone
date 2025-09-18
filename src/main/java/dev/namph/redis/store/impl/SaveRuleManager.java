package dev.namph.redis.store.impl;

import java.util.List;

public class SaveRuleManager {
    public static class SaveRule {
        private final int seconds;
        private final int changes;

        public SaveRule(int seconds, int changes) {
            this.seconds = seconds;
            this.changes = changes;
        }

        public int getSeconds() {
            return seconds;
        }

        public int getChanges() {
            return changes;
        }
    }

    private final List<SaveRule> saveRules;
    private final int DEFAULT_SAVE_1_SECONDS = 900;
    private final int DEFAULT_SAVE_1_CHANGES = 1;
    private final int DEFAULT_SAVE_2_SECONDS = 300;
    private final int DEFAULT_SAVE_2_CHANGES = 10;
    private final int DEFAULT_SAVE_3_SECONDS = 60;
    private final int DEFAULT_SAVE_3_CHANGES = 1000;

    public SaveRuleManager() {
        this.saveRules = new java.util.ArrayList<>();
        // Default rules
        addRule(DEFAULT_SAVE_1_SECONDS, DEFAULT_SAVE_1_CHANGES);
        addRule(DEFAULT_SAVE_2_SECONDS, DEFAULT_SAVE_2_CHANGES);
        addRule(DEFAULT_SAVE_3_SECONDS, DEFAULT_SAVE_3_CHANGES);
    }

    private void addRule(int seconds, int changes) {
        saveRules.add(new SaveRule(seconds, changes));
    }

    public boolean shouldSave(long changes, long lastSaveTime) {
        long currentTime = System.currentTimeMillis() / 1000; // current time in seconds
        long elapsedTime = currentTime - lastSaveTime / 1000;

        for (SaveRule rule : saveRules) {
            if (elapsedTime >= rule.getSeconds() && changes >= rule.getChanges()) {
                return true;
            }
        }
        return false;
    }
}
