package com.crawler.profiler;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.time.Clock;

public final class ProfilerModule extends AbstractModule {
    @Provides
    @Singleton
    Profiler provideProfiler(Clock clock) {
        return new ProfilerImpl(clock);
    }
}
