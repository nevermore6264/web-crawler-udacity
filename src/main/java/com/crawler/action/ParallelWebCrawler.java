package com.crawler.action;

import com.crawler.json.CrawlResult;
import com.crawler.json.CrawlResultBuilder;
import com.crawler.parser.PageParserFactory;
import com.crawler.qualifier.IgnoredUrls;
import com.crawler.qualifier.MaxDepth;
import com.crawler.qualifier.PopularWordCount;
import com.crawler.qualifier.TargetParallelism;
import com.crawler.qualifier.Timeout;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;

public final class ParallelWebCrawler implements WebCrawler {
    private final Clock clock;
    private final PageParserFactory parserFactory;
    private final Duration timeout;
    private final int popularWordCount;
    private final ForkJoinPool pool;
    private final int maxDepth;
    private final List<Pattern> ignoredUrls;

    @Inject
    ParallelWebCrawler(
            Clock clock,
            PageParserFactory parserFactory,
            @Timeout Duration timeout,
            @PopularWordCount int popularWordCount,
            @TargetParallelism int threadCount,
            @MaxDepth int maxDepth,
            @IgnoredUrls List<Pattern> ignoredUrls) {
        this.clock = clock;
        this.parserFactory = parserFactory;
        this.timeout = timeout;
        this.popularWordCount = popularWordCount;
        this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
        this.maxDepth = maxDepth;
        this.ignoredUrls = ignoredUrls;
    }

    @Override
    public CrawlResult crawl(List<String> startingUrls) {
        Instant deadline = clock.instant().plus(timeout);
        Map<String, Integer> counts = new ConcurrentHashMap<>();
        Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());
        for (String url : startingUrls) {
            CrawlPageAction.Builder taskBuilder = new CrawlPageAction.Builder()
                    .setUrl(url)
                    .setDeadline(deadline)
                    .setMaxDepth(maxDepth)
                    .setCounts(counts)
                    .setVisitedUrls(visitedUrls)
                    .setClock(clock)
                    .setIgnoredUrls(ignoredUrls)
                    .setParserFactory(parserFactory);
            CrawlPageAction newTask = taskBuilder.build();
            pool.invoke(newTask);
        }
        pool.shutdown();

        if (counts.isEmpty()) {
            CrawlResultBuilder crawlResultBuilder = new CrawlResultBuilder();
            crawlResultBuilder.setWordCounts(counts);
            crawlResultBuilder.setUrlsVisited(visitedUrls.size());
            return crawlResultBuilder.build();
        }

        CrawlResultBuilder crawlResultBuilder = new CrawlResultBuilder();
        crawlResultBuilder.setWordCounts(WordCounts.sort(counts, popularWordCount));
        crawlResultBuilder.setUrlsVisited(visitedUrls.size());
        return crawlResultBuilder.build();
    }

    @Override
    public int getMaxParallelism() {
        return Runtime.getRuntime().availableProcessors();
    }
}
