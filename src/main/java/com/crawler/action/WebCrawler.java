package com.crawler.action;

import com.crawler.json.CrawlResult;
import com.crawler.qualifier.Profiled;

import java.util.List;

public interface WebCrawler {

    @Profiled
    CrawlResult crawl(List<String> startingUrls);

    default int getMaxParallelism() {
        return 1;
    }
}
