package com.crawler.action;

import com.crawler.json.CrawlResult;
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

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
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
        // Define concurrent data structures for counting of words and visited URLs
        Map<String, Integer> counts = new ConcurrentHashMap<>();
        Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());
        // Create initial tasks for each starting URL
        for (String url : startingUrls) {
            var taskBuilder = new CrawlPageAction.Builder()
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
            return new CrawlResult.Builder()
                    .setWordCounts(counts)
                    .setUrlsVisited(visitedUrls.size())
                    .build();
        }

        return new CrawlResult.Builder()
                .setWordCounts(WordCounts.sort(counts, popularWordCount))
                .setUrlsVisited(visitedUrls.size())
                .build();
    }

    @Override
    public int getMaxParallelism() {
        return Runtime.getRuntime().availableProcessors();
    }
}
