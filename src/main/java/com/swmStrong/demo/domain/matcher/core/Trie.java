package com.swmStrong.demo.domain.matcher.core;

import org.bson.types.ObjectId;

import java.util.HashMap;

public class Trie {
    static class Node {
        char key;
        HashMap<Character, Node> children = new HashMap<>();
        ObjectId category;
        int count;

        public Node(char key) {
            this.key = key;
        }

        public Node() {}
    }

    Node root = new Node();

    public void insert(ObjectId category, String pattern) {
        Node now = this.root;
        for (char c: pattern.toCharArray()) {
            now = now.children.computeIfAbsent(c, Node::new);
            now.count++;
        }
        now.category = category;
    }

    public ObjectId search(String pattern) {
        return search(pattern, true);
    }

    public ObjectId search(String pattern, boolean prefixMatch) {
        Node now = this.root;
        for (char c: pattern.toCharArray()) {
            now = now.children.get(c);
            if (now == null) return null;
            if (prefixMatch && now.category != null) return now.category;
        }
        return now.category;
    }

    public void remove(String pattern) {
        Node now = this.root;
        for (char c: pattern.toCharArray()) {
            now = now.children.get(c);
            if (now == null) return;
            now.count--;
            if (now.count == 0) {
                now.children = new HashMap<>();
                return;
            }
        }
        now.category = null;
    }
}
