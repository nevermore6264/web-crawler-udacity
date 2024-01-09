package com.crawler.action;

import com.crawler.json.CrawlResult;
import com.crawler.qualifier.Profiled;

import java.util.List;

public interface WebCrawler {

    Integer ONE = 1;

    @Profiled
    CrawlResult crawl(List<String> startingUrls);

    default int getMaxParallelism() {
        return ONE;
    }
}
