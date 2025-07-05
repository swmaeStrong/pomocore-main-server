package com.swmStrong.demo.domain.common.util;

import java.util.HashMap;

public class Trie<T> {
    static class Node<T> {
        char key;
        HashMap<Character, Node<T>> children = new HashMap<>();
        T value;
        int count;

        public Node(char key) {
            this.key = key;
        }

        public Node() {}
    }

    private Node<T> root = new Node<>();

    public void insert(T value, String pattern) {
        Node<T> now = this.root;
        for (char c: pattern.toLowerCase().toCharArray()) {
            now = now.children.computeIfAbsent(c, Node::new);
            now.count++;
        }
        now.value = value;
    }

    public T search(String pattern, boolean prefixMatch) {
        Node<T> now = this.root;
        for (char c: pattern.toLowerCase().toCharArray()) {
            now = now.children.get(c);
            if (now == null) return null;
            if (prefixMatch && now.value != null) return now.value;
        }
        return now.value;
    }

    public void remove(String pattern) {
        Node<T> now = this.root;
        for (char c: pattern.toLowerCase().toCharArray()) {
            now = now.children.get(c);
            if (now == null) return;
            now.count--;
            if (now.count == 0) {
                now.children = new HashMap<>();
                return;
            }
        }
        now.value = null;
    }
}
