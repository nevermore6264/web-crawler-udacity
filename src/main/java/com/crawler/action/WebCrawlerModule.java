package com.crawler.action;

import com.crawler.json.CrawlerConfig;
import com.crawler.parser.ParserModule;
import com.crawler.profiler.Profiler;
import com.crawler.qualifier.IgnoredUrls;
import com.crawler.qualifier.MaxDepth;
import com.crawler.qualifier.PopularWordCount;
import com.crawler.qualifier.TargetParallelism;
import com.crawler.qualifier.Timeout;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.ProvisionException;
import com.google.inject.multibindings.Multibinder;

import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class WebCrawlerModule extends AbstractModule {

    private final CrawlerConfig config;

    public WebCrawlerModule(CrawlerConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    @Override
    protected void configure() {
        Multibinder<WebCrawler> webCrawlerMultibinder =
                Multibinder.newSetBinder(binder(), WebCrawler.class, Internal.class);
        webCrawlerMultibinder.addBinding().to(SequentialWebCrawler.class);
        webCrawlerMultibinder.addBinding().to(ParallelWebCrawler.class);

        bind(Clock.class).toInstance(Clock.systemUTC());
        bind(Key.get(Integer.class, MaxDepth.class)).toInstance(config.getMaxDepth());
        bind(Key.get(Integer.class, PopularWordCount.class)).toInstance(config.getPopularWordCount());
        bind(Key.get(Duration.class, Timeout.class)).toInstance(config.getTimeout());
        bind(new Key<List<Pattern>>(IgnoredUrls.class) {
        }).toInstance(config.getIgnoredUrls());

        install(
                new ParserModule.Builder()
                        .setTimeout(config.getTimeout())
                        .setIgnoredWords(config.getIgnoredWords())
                        .build());
    }

    @Provides
    @Singleton
    @Internal
    WebCrawler provideRawWebCrawler(
            @Internal Set<WebCrawler> implementations,
            @TargetParallelism int targetParallelism) {
        String override = config.getImplementationOverride();
        if (!override.isEmpty()) {
            return implementations
                    .stream()
                    .filter(impl -> impl.getClass().getName().equals(override))
                    .findFirst()
                    .orElseThrow(() -> new ProvisionException("Implementation not found: " + override));
        }
        return implementations
                .stream()
                .filter(impl -> targetParallelism <= impl.getMaxParallelism())
                .findFirst()
                .orElseThrow(
                        () -> new ProvisionException(
                                "No implementation able to handle parallelism = \"" +
                                        config.getParallelism() + "\"."));
    }

    @Provides
    @Singleton
    @TargetParallelism
    int provideTargetParallelism() {
        if (config.getParallelism() >= 0) {
            return config.getParallelism();
        }
        return Runtime.getRuntime().availableProcessors();
    }

    @Provides
    @Singleton
    WebCrawler provideWebCrawlerProxy(Profiler wrapper, @Internal WebCrawler delegate) {
        return wrapper.wrap(WebCrawler.class, delegate);
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Internal {
    }
}