package dev.namph.redis.store.impl;

public class SkipList {
    private final static int MAX_LEVEL = 32;
    private Node head;
    private Node tail;
    private int level;
    private int size;
    private OASet<Node> set;
    private final static double P = 0.25;

    private static class Node {
        private Key key;
        private double score;
        private Node backward;
        private Level[] levels;

        Node(Key key, double score, int height) {
            this.key = key;
            this.score = score;
            levels = new Level[height];
            for (int i = 0; i < height; i++) {
                levels[i] = new Level(0, null);
            }
        }
    }

    private static class Level {
        int span;
        Node forward;

        public Level(int span, Node node) {
            this.span = span;
            this.forward = node;
        }
    }

    public SkipList() {
        this.head = new Node(new Key(new byte[0]), Double.NEGATIVE_INFINITY, MAX_LEVEL);
        this.level = 1;
        this.size = 0;
        this.set = new OASet<>();
    }

    public void addOrUpdate (Key key, double score) {
        int lvl = randomLevel();
        Node newNode = new Node(key, score, lvl);
        Node current = head;
        Node[] update = new Node[MAX_LEVEL];
        int[] spans = new int[MAX_LEVEL];

        for (int i = level - 1; i >= 0; i--) {
            spans[i] = i == level - 1 ? 1 : current.levels[i + 1].span;
            while (current.levels[i].forward != null && current.levels[i].forward.score < score) {
                spans[i] += current.levels[i].span;
                current = current.levels[i].forward;
            }
            update[i] = current;
        }

        // if lvl is greater than the current level, initialize new levels
        if (lvl > level) {
            for (int i = level; i < lvl; i++) {
                update[i] = head;
                spans[i] = size + 1; // span for new levels is size + 1
            }
            level = lvl;
        }

        for (int i = 0; i < newNode.levels.length; i++) {
            newNode.levels[i].forward = update[i].levels[i].forward;

            update[i].levels[i].forward = newNode;
            // span math
            newNode.levels[i].span = spans[i];
            if (i > 0) {
                int after = update[i].levels[i].span - (spans[0] - spans[i]);
                newNode.levels[i].span = Math.max(after, 0);
                update[i].levels[i].span = spans[0] - spans[i] + 1;
            }
        }

        newNode.backward = update[0] == head ? null : update[0];
        if (newNode.levels[0].forward != null) {
            newNode.levels[0].forward.backward = newNode;
        } else {
            tail = newNode; // update tail if this is the last node
        }

        size++;
        set.add(newNode);
    }

    private int randomLevel() {
        int lvl = 1;
        while (Math.random() < P && lvl < MAX_LEVEL) {
            lvl++;
        }
        return lvl;
    }
}
