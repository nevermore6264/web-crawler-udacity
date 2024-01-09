package com.crawler.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CrawlResultBuilder {

    private Map<String, Integer> wordFrequencies = new HashMap<>();
    private int pageCount;

    public void setWordCounts(Map<String, Integer> wordCounts) {
        this.wordFrequencies = Objects.requireNonNull(wordCounts);
    }

    public void setUrlsVisited(int pageCount) {
        this.pageCount = pageCount;
    }

    public CrawlResult build() {
        return new CrawlResult(Collections.unmodifiableMap(wordFrequencies), pageCount);
    }
}
