package com.crawler.action;

import com.crawler.json.CrawlResult;
import com.crawler.json.CrawlResultBuilder;
import com.crawler.parser.PageParser;
import com.crawler.parser.PageParserFactory;
import com.crawler.qualifier.IgnoredUrls;
import com.crawler.qualifier.MaxDepth;
import com.crawler.qualifier.PopularWordCount;
import com.crawler.qualifier.Timeout;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class SequentialWebCrawler implements WebCrawler {

    private final Clock clock;
    private final PageParserFactory parserFactory;
    private final Duration timeout;
    private final int popularWordCount;
    private final int maxDepth;
    private final List<Pattern> ignoredUrls;

    @Inject
    SequentialWebCrawler(
            Clock clock,
            PageParserFactory parserFactory,
            @Timeout Duration timeout,
            @PopularWordCount int popularWordCount,
            @MaxDepth int maxDepth,
            @IgnoredUrls List<Pattern> ignoredUrls) {
        this.clock = clock;
        this.parserFactory = parserFactory;
        this.timeout = timeout;
        this.popularWordCount = popularWordCount;
        this.maxDepth = maxDepth;
        this.ignoredUrls = ignoredUrls;
    }

    @Override
    public CrawlResult crawl(List<String> startingUrls) {
        Instant deadline = clock.instant().plus(timeout);
        Map<String, Integer> counts = new HashMap<>();
        Set<String> visitedUrls = new HashSet<>();
        for (String url : startingUrls) {
            crawlInternal(url, deadline, maxDepth, counts, visitedUrls);
        }

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

    private void crawlInternal(
            String url,
            Instant deadline,
            int maxDepth,
            Map<String, Integer> counts,
            Set<String> visitedUrls) {
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return;
        }
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return;
            }
        }
        if (visitedUrls.contains(url)) {
            return;
        }
        visitedUrls.add(url);
        PageParser.Result result = parserFactory.get(url).parse();
        for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
            String key = e.getKey();
            Integer value = e.getValue();
            if (counts.containsKey(key)) {
                counts.put(key, value + counts.get(key));
            } else {
                counts.put(key, value);
            }
        }
        for (String link : result.getLinks()) {
            crawlInternal(link, deadline, maxDepth - 1, counts, visitedUrls);
        }
    }
}
