package com.swmStrong.demo.domain.matcher.core;

import com.swmStrong.demo.domain.patternCategory.entity.PatternCategory;
import com.swmStrong.demo.domain.patternCategory.repository.PatternCategoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class PatternMatcher {

    private final PatternCategoryRepository patternCategoryRepository;

    public PatternMatcher(PatternCategoryRepository patternCategoryRepository) {
        this.patternCategoryRepository = patternCategoryRepository;
        this.root = new Node();
    }

    @PostConstruct
    public void init() {
        this.root = new Node();

        List<PatternCategory> allCategories = patternCategoryRepository.findAll();
        for (PatternCategory patternCategory: allCategories) {
            for (String pattern: patternCategory.getPatterns()) {
                insert(pattern, patternCategory.getCategory());
            }
        }
        connect();
        log.info("AhoCorasick PatternMatcher Initialized");
    }

    static class Node {
        char key;
        HashMap<Character, Node> children = new HashMap<>();
        Node fail = null;
        Set<String> categories = new HashSet<>();

        public Node(char key) {
            this.key = key;
        }

        public Node() {
            this.fail = this;
        }
    }

    Node root;

    public void insert(String pattern, String category) {
        Node now = this.root;

        for (char c: pattern.toCharArray()) {
            now = now.children.computeIfAbsent(c, Node::new);
        }
        now.categories.add(category);
        log.info("insert pattern: {} category: {}", pattern, category);
    }

    private void connect() {
        Queue<Node> q = new LinkedList<>();
        q.add(this.root);

        while (!q.isEmpty()) {
            Node now = q.poll();

            for (char c: now.children.keySet()) {
                Node next = now.children.get(c);

                if (now == this.root) {
                    next.fail = this.root;
                } else {
                    Node dst = now.fail;
                    while (dst != this.root && !dst.children.containsKey(c)) {
                        dst = dst.fail;
                    }
                    if (dst.children.containsKey(c)) {
                        dst = dst.children.get(c);
                    }
                    next.fail = dst;
                }
                next.categories.addAll(next.fail.categories);

                q.offer(next);
            }
        }
    }

    public Set<String> search(String title) {
        Node now = this.root;

        Set<String> matchedCategories = new HashSet<>();
        for (char c: title.toCharArray()) {
            while (now != this.root && !now.children.containsKey(c)) {
                now = now.fail;
            }
            if (now.children.containsKey(c)) {
                now = now.children.get(c);
            }

            Node temp = now ;
            while (temp != this.root) {
                matchedCategories.addAll(temp.categories);
                temp = temp.fail;
            }
        }
        return matchedCategories;
    }
}
