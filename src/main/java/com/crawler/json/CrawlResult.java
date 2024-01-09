package com.crawler.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class CrawlResult {

    private final Map<String, Integer> wordCounts;
    private final int urlsVisited;

    public CrawlResult(Map<String, Integer> wordCounts, int urlsVisited) {
        this.wordCounts = wordCounts;
        this.urlsVisited = urlsVisited;
    }

    public Map<String, Integer> getWordCounts() {
        return wordCounts;
    }

    public int getUrlsVisited() {
        return urlsVisited;
    }

}