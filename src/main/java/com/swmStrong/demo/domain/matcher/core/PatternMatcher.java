package com.swmStrong.demo.domain.matcher.core;

import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import com.swmStrong.demo.domain.categoryPattern.repository.CategoryPatternRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component("PatternMatcher")
public class PatternMatcher {

    private final CategoryPatternRepository categoryPatternRepository;

    public PatternMatcher(CategoryPatternRepository categoryPatternRepository) {
        this.categoryPatternRepository = categoryPatternRepository;
    }

    Node root = new Node();

    @PostConstruct
    public void init() {
        this.root = new Node();

        List<CategoryPattern> allCategories = categoryPatternRepository.findAll();
        for (CategoryPattern categoryPattern : allCategories) {
            if (categoryPattern.getPatterns() == null || categoryPattern.getPatterns().isEmpty()) {
                continue;
            }
            for (String pattern: categoryPattern.getPatterns()) {
                insert(pattern, categoryPattern.getId());
            }
        }
        connect();
        log.info("AhoCorasick PatternMatcher Initialized");
    }

    static class Node {
        char key;
        HashMap<Character, Node> children = new HashMap<>();
        Node fail = null;
        Set<ObjectId> categories = new HashSet<>();

        public Node(char key) {
            this.key = key;
        }

        public Node() {
            this.fail = this;
        }
    }


    public void insert(String pattern, ObjectId categoryId) {
        Node now = this.root;
        pattern = pattern.toLowerCase();
        for (char c: pattern.toCharArray()) {
            now = now.children.computeIfAbsent(c, Node::new);
        }
        now.categories.add(categoryId);
        log.info("insert pattern: {} categoryId: {}", pattern, categoryId);
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

    public Set<ObjectId> search(String title) {
        Node now = this.root;
        title = title.toLowerCase();
        Set<ObjectId> matchedCategories = new HashSet<>();
        for (char c: title.toCharArray()) {

            while (now != null && now != this.root && !now.children.containsKey(c)) {
                now = now.fail;
            }

            if (now != null && now.children.containsKey(c)) {
                now = now.children.get(c);
            }

            Node temp = now;
            while (temp != this.root && temp != null) {
                if (!temp.categories.isEmpty()) {
                    matchedCategories.addAll(temp.categories);
                }
                temp = temp.fail;
            }
        }
        return matchedCategories;
    }
}
