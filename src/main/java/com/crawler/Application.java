package com.crawler;

import com.crawler.action.WebCrawler;
import com.crawler.action.WebCrawlerModule;
import com.crawler.json.Config;
import com.crawler.profiler.Profiler;
import com.crawler.profiler.ProfilerModule;
import com.google.inject.Guice;
import com.crawler.json.CrawlResult;
import com.crawler.json.CrawlResultWriter;
import com.crawler.json.CrawlerConfig;

import javax.inject.Inject;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Objects;

public final class Application {

    private final CrawlerConfig config;

    private Application(CrawlerConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    @Inject
    private WebCrawler crawler;

    @Inject
    private Profiler profiler;

    private void execute() throws Exception {
        Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

        CrawlResult result = crawler.crawl(config.getStartPages());
        CrawlResultWriter resultWriter = new CrawlResultWriter(result);
        String resultPathString = config.getResultPath();

        try (OutputStreamWriter standardOutputWriter = new OutputStreamWriter(System.out)) {
            if (!resultPathString.isEmpty()) {
                Path resultPath = Path.of(resultPathString);
                resultWriter.write(resultPath);
            } else {
                resultWriter.write(standardOutputWriter);
                standardOutputWriter.write(System.lineSeparator());
            }
            String profileOutputString = config.getProfileOutputPath();
            if (!profileOutputString.isEmpty()) {
                Path profileOutputPath = Path.of(profileOutputString);
                profiler.writeData(profileOutputPath);
            } else {
                profiler.writeData(standardOutputWriter);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        CrawlerConfig config = new Config(Path.of(args[0])).load();
        new Application(config).execute();
    }
}