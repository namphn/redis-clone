package dev.namph.redis.store.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkipList<T> {
    private final static int MAX_LEVEL = 32;
    private Node<T> head;
    private Node<T> tail;
    private int level;
    private int size;
    private final static double P = 0.25;

    public static class Node<T> {
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

        public T getKey() {
            return key;
        }

        public double getScore() {
            return score;
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

    @SuppressWarnings( "unchecked")
    public void add(T key, double score) {
        int lvl = randomLevel();
        Node<T> newNode = new Node<>(key, score, lvl);
        Node<T> current = head;
        Node<T>[] update = new Node[MAX_LEVEL];
        int[] spans = new int[MAX_LEVEL];

        for (int i = level - 1; i >= 0; i--) {
            spans[i] = i == level - 1 ? 0 : spans[i + 1];
            while (lessThan(current.levels[i].forward, newNode)) {
                spans[i] += current.levels[i].span;
                current = current.levels[i].forward;
            }
            update[i] = current;
        }

        // if lvl is greater than the current level, initialize new levels
        if (lvl > level) {
            for (int i = level; i < lvl; i++) {
                update[i] = head;
                spans[i] = 0;
                update[i].levels[i].span = size + 1;
            }
            level = lvl;
        }

        for (int i = 0; i < newNode.levels.length; i++) {
            newNode.levels[i].forward = update[i].levels[i].forward;

            update[i].levels[i].forward = newNode;
            // span math
            int after = update[i].levels[i].span - (spans[0] - spans[i]);
            newNode.levels[i].span = Math.max(after,0);
            update[i].levels[i].span = spans[0] - spans[i] + 1;
        }

        // For untouched higher levels, we jumped over one more node
        for (int i = lvl; i < level; i++) {
            update[i].levels[i].span += 1;
        }

            newNode.backward = update[0] == head ? null : update[0];
        if (newNode.levels[0].forward != null) {
            newNode.levels[0].forward.backward = newNode;
        } else {
            tail = newNode; // update tail if this is the last node
        }

        size++;
    }

    @SuppressWarnings("unchecked")
    public void remove(T key, double score) {
        Node<T> current = head;
        Node<T>[] update = new Node[MAX_LEVEL];

        for (int i = level - 1; i >= 0; i--) {
            while (current.levels[i].forward != null && lessThan(current.levels[i].forward, new Node<>(key, score, 0))) {
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

    public List<Node<T>> getByScore(double minScore, double maxScore, int offset, int limit) {
        Node<T> current = head;
        List<Node<T>> result = new java.util.ArrayList<>();
        int count = 0;

        for (int i = level - 1; i >= 0; i--) {
            while (current.levels[i].forward != null && current.levels[i].forward.score < minScore) {
                current = current.levels[i].forward;
            }
        }

        while (current != null && current.score <= maxScore) {
            if (current.score >= minScore) {
                if (count >= offset && limit != 0 && result.size() < limit) {
                    result.add(current);
                }
                count++;
            }
            current = current.levels[0].forward;
        }

        return result;
    }

    public List<Node<T>> getReverseByScore(double minScore, double maxScore, int offset, int limit) {
        Node<T> current = tail;
        for (int i = level - 1; i >= 0; i--) {
            while (current != null && current.levels[i].forward != null
                    && current.levels[i].forward.score <= maxScore) {
                current = current.levels[i].forward;
            }
        }

        List<Node<T>> result = new java.util.ArrayList<>();
        int count = 0;
        while (current != null && current.score >= minScore) {
            if (count >= offset && limit != 0 && result.size() < limit) {
                result.add(current);
                count++;
                current = current.backward;
            } else {
                break;
            }
        }

        return result;
    }

    @SuppressWarnings( "unchecked")
    public List<Node<T>> getByRank(int start, int end, int limit, int offset) {
        if (start < 0 || end < 0 || start > end) {
            throw new IllegalArgumentException("Invalid range values");
        }
        Node<T> current = head;
        int tranvelsal = 0;
        for (int i = level - 1; i >= 0; i--) {
            while (current.levels[i].forward != null && current.levels[i].span + tranvelsal <= start) {
                current = current.levels[i].forward;
                tranvelsal += current.levels[i].span;
            }
        }

        List<Node<T>> result = new java.util.ArrayList<>();
        int count = 0;
        while (current != null && tranvelsal <= end) {
            if (count >= offset && (limit == 0 || result.size() < limit)) {
                result.add(current);
            }
            count++;
            current = current.levels[0].forward;
            tranvelsal++;
        }
        return result;
    }

    @SuppressWarnings( "unchecked" )
    public List<Node<T>> getByAlphabetical(T start, T end, int limit, int offset, boolean includeStart, boolean includeEnd) {
        if (start == null|| end == null) {
            throw new IllegalArgumentException("Invalid range values");
        }
        Node<T> current = head;
        double firstScore = head.levels[0].forward.score;
        for (int i = level - 1; i >= 0; i--) {
            while (current.levels[i].forward != null && lessThan(current.levels[i].forward, new Node<T>(start, firstScore, 0))) {
                current = current.levels[i].forward;
            }
        }
        // If includeStart is false, skip the start node if it matches
        if (!includeStart) {
            while (current.key.equals(start)) {
                current = current.levels[0].forward;
            }
        }

        List<Node<T>> result = new ArrayList<>();
        int count = 0;
        while (current != null && lessThan(current, new Node<T>(end, firstScore, 0))) {
            if (count >= offset && limit != 0 && result.size() < limit) {
                result.add(current);
                count++;
            }
            current = current.levels[0].forward;
        }
        if (includeEnd) {
            // If includeEnd is true, check if the last node matches the end condition
            while (current != null && current.key.equals(end)) {
                result.add(current);
            }
        }
        return result;
    }

    public List<Node<T>> getReverseByAlphabetical(T start, T end, int limit, int offset, boolean includeStart, boolean includeEnd) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Invalid range values");
        }
        Node<T> current = tail;
        double lastScore = tail.levels[0].forward.score;
        for (int i = level - 1; i >= 0; i--) {
            while (current != null && lessThan(new Node<T>(end, lastScore, 0), current.levels[i].forward)) {
                current = current.levels[i].forward;
            }
        }
        // If includeStart is false, skip the start node if it matches
        if (!includeEnd) {
            while (current.key.equals(start)) {
                current = current.backward;
            }
        }

        List<Node<T>> result = new ArrayList<>();
        int count = 0;
        while (current != null && lessThan(new Node<T>(start, lastScore, 0), current)) {
            if (count >= offset && limit != 0 && result.size() < limit) {
                result.add(current);
                count++;
            }
            current = current.backward;
        }
        if (includeStart) {
            // If includeEnd is true, check if the last node matches the end condition
            while (current != null && current.key.equals(start)) {
                result.add(current);
            }
        }
        return result;
    }

    private boolean lessThan(Node<T> node1, Node<T> node2) {
        if (node1 == null || node2 == null) {
            return false;
        }
        int cm = Double.compare(node1.score, node2.score);
        if (cm == 0) {
            if (node1.key instanceof Key key1 && node2.key instanceof Key key2) {
                return Arrays.compare(key1.getVal(), key2.getVal()) < 0;
            } else {
                return node1.key.toString().compareTo(node2.key.toString()) < 0;
            }
        }
        return cm < 0;
    }

    private int randomLevel() {
        int lvl = 1;
        while (Math.random() < P && lvl < MAX_LEVEL) {
            lvl++;
        }
        return lvl;
    }
}
