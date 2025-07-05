package com.swmStrong.demo.domain.common.util.badWords;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import lombok.extern.slf4j.Slf4j;

import java.util.TreeMap;

@Slf4j
public class BadWordsFilter {
    private static final String[] badWords = BadWords.list;
    private static final AhoCorasickDoubleArrayTrie<String> badWordsMatcher;
    static {
        badWordsMatcher = new AhoCorasickDoubleArrayTrie<>();
        TreeMap<String, String> map = new TreeMap<>();
        for (String badWord: badWords) {
            map.put(badWord, badWord);
        }
        badWordsMatcher.build(map);
        log.trace("욕설 필터 장착 완료");
    }

    public static boolean isBadWord(String word) {
        return badWordsMatcher.matches(word);
    }
}
