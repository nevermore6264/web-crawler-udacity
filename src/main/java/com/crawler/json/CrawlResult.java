package com.crawler.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class CrawlResult {

    private final Map<String, Integer> wordCounts;
    private final int urlsVisited;

    private CrawlResult(Map<String, Integer> wordCounts, int urlsVisited) {
        this.wordCounts = wordCounts;
        this.urlsVisited = urlsVisited;
    }

    public Map<String, Integer> getWordCounts() {
        return wordCounts;
    }

    public int getUrlsVisited() {
        return urlsVisited;
    }

    public static final class Builder {
        private Map<String, Integer> wordFrequencies = new HashMap<>();
        private int pageCount;

        public Builder setWordCounts(Map<String, Integer> wordCounts) {
            this.wordFrequencies = Objects.requireNonNull(wordCounts);
            return this;
        }

        public Builder setUrlsVisited(int pageCount) {
            this.pageCount = pageCount;
            return this;
        }

        public CrawlResult build() {
            return new CrawlResult(Collections.unmodifiableMap(wordFrequencies), pageCount);
        }
    }
}