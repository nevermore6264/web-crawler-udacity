package com.crawler.action;

import com.crawler.parser.PageParser;
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
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Pattern;

public final class CrawlPageAction extends RecursiveAction {
    private final String url;
    private final Instant deadline;
    private final int maxDepth;
    private final Map<String, Integer> counts;
    private final Set<String> visitedUrls;
    private final Clock clock;
    private final List<Pattern> ignoredUrls;
    private final PageParserFactory parserFactory;

    private CrawlPageAction(String url, Instant deadline, int maxDepth,
                            Map<String, Integer> counts, Set<String> visitedUrls, Clock clock,
                            List<Pattern> ignoredUrls, PageParserFactory parserFactory
    ) {
        this.url = url;
        this.deadline = deadline;
        this.maxDepth = maxDepth;
        this.counts = counts;
        this.visitedUrls = visitedUrls;
        this.clock = clock;
        this.ignoredUrls = ignoredUrls;
        this.parserFactory = parserFactory;
    }

    @Override
    public void compute() {
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return;
        }
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return;
            }
        }
        synchronized (visitedUrls) {
            if (visitedUrls.contains(url)) {
                return;
            }
            visitedUrls.add(url);
        }
        PageParser.Result result = parserFactory.get(url).parse();
        for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
            synchronized (counts) {
                if (counts.containsKey(e.getKey())) {
                    counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
                } else {
                    counts.put(e.getKey(), e.getValue());
                }
            }
        }
        List<CrawlPageAction> subtasks = new ArrayList<>();
        for (String link : result.getLinks()) {
            var taskBuilder = new CrawlPageAction.Builder();
            taskBuilder
                    .setUrl(link)
                    .setDeadline(deadline)
                    .setMaxDepth(maxDepth - 1)
                    .setCounts(counts)
                    .setVisitedUrls(visitedUrls)
                    .setClock(clock)
                    .setIgnoredUrls(ignoredUrls)
                    .setParserFactory(parserFactory);
            CrawlPageAction newTask = taskBuilder.build();
            subtasks.add(newTask);
        }
        try {
            invokeAll(subtasks);
        } catch (NullPointerException e) {
            System.out.println("One of sub-tasks is null");
            e.printStackTrace();
        } catch (RejectedExecutionException e) {
            System.out.println("One of subtasks cannot be scheduled for execution");
            e.printStackTrace();
        }
    }

    public static final class Builder {
        private String url = "";
        private Instant deadline = Instant.EPOCH;
        private int maxDepth = 0;
        private Map<String, Integer> counts = new ConcurrentHashMap<>();
        private Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());
        private Clock clock = Clock.systemUTC();
        private List<Pattern> ignoredUrls = new ArrayList<>();
        private PageParserFactory parserFactory = null;

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setDeadline(Instant deadline) {
            this.deadline = deadline;
            return this;
        }

        public Builder setMaxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        public Builder setCounts(Map<String, Integer> counts) {
            this.counts = counts;
            return this;
        }

        public Builder setVisitedUrls(Set<String> visitedUrls) {
            this.visitedUrls = visitedUrls;
            return this;
        }

        public Builder setClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder setIgnoredUrls(List<Pattern> ignoredUrls) {
            this.ignoredUrls = ignoredUrls;
            return this;
        }

        public Builder setParserFactory(PageParserFactory parserFactory) {
            this.parserFactory = parserFactory;
            return this;
        }

        public CrawlPageAction build() {
            return new CrawlPageAction(url, deadline, maxDepth, counts, visitedUrls, clock,
                    ignoredUrls, parserFactory);
        }
    }
}
