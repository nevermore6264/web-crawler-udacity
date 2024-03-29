package com.crawler.profiler;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

final class ProfilingState {
    private final Map<String, Duration> data = new ConcurrentHashMap<>();

    void record(Class<?> callingClass, Method method, Duration elapsed) {
        Objects.requireNonNull(callingClass);
        Objects.requireNonNull(method);
        Objects.requireNonNull(elapsed);
        if (elapsed.isNegative()) {
            throw new IllegalArgumentException("negative elapsed time");
        }
        String key = formatMethodCall(callingClass, method);
        data.compute(key, (k, v) -> (v == null) ? elapsed : v.plus(elapsed));
    }

    void write(Writer writer) throws IOException {
        List<Map.Entry<String, Duration>> toSort = new ArrayList<>();
        for (Map.Entry<String, Duration> e : data.entrySet()) {
            toSort.add(e);
        }
        toSort.sort(Map.Entry.comparingByKey());
        List<String> entries =
                new ArrayList<>();
        for (Map.Entry<String, Duration> e : toSort) {
            String s = e.getKey() + " took " + formatDuration(e.getValue()) + System.lineSeparator();
            entries.add(s);
        }
        for (String entry : entries) {
            writer.write(entry);
        }
    }

    private static String formatMethodCall(Class<?> callingClass, Method method) {
        return String.format("%s#%s", callingClass.getName(), method.getName());
    }

    private static String formatDuration(Duration duration) {
        return String.format(
                "%sm %ss %sms", duration.toMinutes(), duration.toSecondsPart(), duration.toMillisPart());
    }
}
