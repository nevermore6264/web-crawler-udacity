package com.crawler.action;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class WordCounts {

    private WordCounts() {
    }

    static Map<String, Integer> sort(Map<String, Integer> wordCounts, int popularWordCount) {
        return wordCounts.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed()
                        .thenComparing(Map.Entry::getKey, Comparator.comparingInt(String::length).reversed())
                        .thenComparing(Map.Entry::getKey))
                .limit(popularWordCount)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (x, y) -> y,
                        LinkedHashMap::new
                ));
    }
}