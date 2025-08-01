package dev.namph.redis.store.impl;

public class SkipList<T> {
    private final static int MAX_LEVEL = 32;
    private Node<T> head;
    private Node<T> tail;
    private int level;
    private int size;
    private final static double P = 0.25;

    private static class Node<T> {
        private final T key;
        private final double score;
        private Node backward;
        private final Level[] levels;

        Node(T key, double score, int height) {
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
    }

    public void add(T key, double score) {
        int lvl = randomLevel();
        Node<T> newNode = new Node<>(key, score, lvl);
        Node<T> current = head;
        Node<T>[] update = new Node[MAX_LEVEL];
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
    }

    public void remove(T key, double score) {
        Node<T> current = head;
        Node<T>[] update = new Node[MAX_LEVEL];

        for (int i = level - 1; i >= 0; i--) {
            while (current.levels[i].forward != null && current.levels[i].forward.score < score) {
                current = current.levels[i].forward;
            }
            update[i] = current;
        }

        if (current.levels[0].forward != null && current.levels[0].forward.key.equals(key)) {
            Node<T> toRemove = current.levels[0].forward;

            for (int i = 0; i < toRemove.levels.length; i++) {
                if (update[i] == head && toRemove.backward == null) {
                    head = toRemove.levels[i].forward; // remove head
                    head.levels[i].span += toRemove.levels[i].span - 1; // adjust span
                } else {
                    update[i].levels[i].forward = toRemove.levels[i].forward;
                    update[i].levels[i].span += toRemove.levels[i].span - 1; // adjust span
                }
                if (toRemove.levels[i].forward != null) {
                    toRemove.levels[i].forward.backward = update[i];
                } else {
                    tail = update[i]; // update tail if this is the last node
                }
            }

            size--;
        }
    }

    private int randomLevel() {
        int lvl = 1;
        while (Math.random() < P && lvl < MAX_LEVEL) {
            lvl++;
        }
        return lvl;
    }
}
