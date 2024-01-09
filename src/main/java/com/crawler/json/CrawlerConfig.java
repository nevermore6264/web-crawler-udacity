package com.crawler.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

@JsonDeserialize(builder = Builder.class)
public final class CrawlerConfig {

    private final List<String> startPages;
    private final List<Pattern> ignoredUrls;
    private final List<Pattern> ignoredWords;
    private final int parallelism;
    private final String implementationOverride;
    private final int maxDepth;
    private final Duration timeout;
    private final int popularWordCount;
    private final String profileOutputPath;
    private final String resultPath;

    public CrawlerConfig(
            List<String> startPages,
            List<Pattern> ignoredUrls,
            List<Pattern> ignoredWords,
            int parallelism,
            String implementationOverride,
            int maxDepth,
            Duration timeout,
            int popularWordCount,
            String profileOutputPath,
            String resultPath) {
        this.startPages = startPages;
        this.ignoredUrls = ignoredUrls;
        this.ignoredWords = ignoredWords;
        this.parallelism = parallelism;
        this.implementationOverride = implementationOverride;
        this.maxDepth = maxDepth;
        this.timeout = timeout;
        this.popularWordCount = popularWordCount;
        this.profileOutputPath = profileOutputPath;
        this.resultPath = resultPath;
    }

    public List<String> getStartPages() {
        return startPages;
    }

    public List<Pattern> getIgnoredUrls() {
        return ignoredUrls;
    }

    public List<Pattern> getIgnoredWords() {
        return ignoredWords;
    }

    public int getParallelism() {
        return parallelism;
    }

    public String getImplementationOverride() {
        return implementationOverride;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getPopularWordCount() {
        return popularWordCount;
    }

    public String getProfileOutputPath() {
        return profileOutputPath;
    }

    public String getResultPath() {
        return resultPath;
    }
}
