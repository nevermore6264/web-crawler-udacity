package com.crawler.profiler;

import com.crawler.qualifier.Profiled;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

final class ProfilerImpl implements Profiler {

    private final Clock clock;
    private final ProfilingState state = new ProfilingState();
    private final ZonedDateTime startTime;

    @Inject
    ProfilerImpl(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
        this.startTime = ZonedDateTime.now(clock);
    }

    @Override
    public <T> T wrap(Class<T> klass, T delegate) {
        Objects.requireNonNull(klass);
        boolean hasAnnotatedMethod = false;
        for (Method m : klass.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Profiled.class)) {
                hasAnnotatedMethod = true;
                break;
            }
        }
        if (!hasAnnotatedMethod) {
            throw new IllegalArgumentException("Delegate's interface doesn't have a method" +
                    " annotated with @Profiled!");
        }

        @SuppressWarnings("unchecked")
        T proxy = (T) Proxy.newProxyInstance(
                klass.getClassLoader(),
                new Class[]{klass},
                new ProfilingMethodInterceptor<>(clock, state, delegate)
        );
        return proxy;
    }

    @Override
    public void writeData(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writeData(writer);
        }
    }

    @Override
    public void writeData(Writer writer) throws IOException {
        writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
        writer.write(System.lineSeparator());
        state.write(writer);
        writer.write(System.lineSeparator());
    }
}
