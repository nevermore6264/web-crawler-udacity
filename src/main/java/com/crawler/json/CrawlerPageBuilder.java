package com.crawler.json;

import com.crawler.action.CrawlPageAction;
import com.crawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class CrawlerPageBuilder {
    private String url = "";
    private Instant deadline = Instant.EPOCH;
    private int maxDepth = 0;
    private Map<String, Integer> counts = new ConcurrentHashMap<>();
    private Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());
    private Clock clock = Clock.systemUTC();
    private List<Pattern> ignoredUrls = new ArrayList<>();
    private PageParserFactory parserFactory = null;

    public CrawlerPageBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public CrawlerPageBuilder setDeadline(Instant deadline) {
        this.deadline = deadline;
        return this;
    }

    public CrawlerPageBuilder setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public CrawlerPageBuilder setCounts(Map<String, Integer> counts) {
        this.counts = counts;
        return this;
    }

    public CrawlerPageBuilder setVisitedUrls(Set<String> visitedUrls) {
        this.visitedUrls = visitedUrls;
        return this;
    }

    public CrawlerPageBuilder setClock(Clock clock) {
        this.clock = clock;
        return this;
    }

    public CrawlerPageBuilder setIgnoredUrls(List<Pattern> ignoredUrls) {
        this.ignoredUrls = ignoredUrls;
        return this;
    }

    public CrawlerPageBuilder setParserFactory(PageParserFactory parserFactory) {
        this.parserFactory = parserFactory;
        return this;
    }

    public CrawlPageAction build() {
        return new CrawlPageAction(url, deadline, maxDepth, counts, visitedUrls, clock,
                ignoredUrls, parserFactory);
    }
}
