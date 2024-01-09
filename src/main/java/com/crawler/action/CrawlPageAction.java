package com.crawler.action;

import com.crawler.json.CrawlerPageBuilder;
import com.crawler.parser.PageParser;
import com.crawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public CrawlPageAction(String url, Instant deadline, int maxDepth,
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
        synchronized (counts) {
            for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
                if (counts.containsKey(e.getKey())) {
                    counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
                } else {
                    counts.put(e.getKey(), e.getValue());
                }
            }
        }
        List<CrawlPageAction> subtasks = new ArrayList<>();
        for (String link : result.getLinks()) {
            CrawlerPageBuilder crawlerPageBuilder = new CrawlerPageBuilder();
            crawlerPageBuilder
                    .setUrl(link)
                    .setDeadline(deadline)
                    .setMaxDepth(maxDepth - 1)
                    .setCounts(counts)
                    .setVisitedUrls(visitedUrls)
                    .setClock(clock)
                    .setIgnoredUrls(ignoredUrls)
                    .setParserFactory(parserFactory);
            CrawlPageAction newTask = crawlerPageBuilder.build();
            subtasks.add(newTask);
        }
        for (CrawlPageAction subtask : subtasks) {
            try {
                subtask.compute();
            } catch (NullPointerException e) {
                System.out.println("One of sub-tasks is null");
                e.printStackTrace();
            } catch (RejectedExecutionException e) {
                System.out.println("One of subtasks cannot be scheduled for execution");
                e.printStackTrace();
            }
        }
    }
}
