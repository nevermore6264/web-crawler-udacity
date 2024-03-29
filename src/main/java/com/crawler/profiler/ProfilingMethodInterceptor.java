package com.crawler.profiler;

import com.crawler.qualifier.Profiled;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class ProfilingMethodInterceptor<T> implements InvocationHandler {

    private final Clock clock;
    private final ProfilingState state;
    private final T delegate;

    public ProfilingMethodInterceptor(Clock clock, ProfilingState state, T delegate) {
        this.clock = Objects.requireNonNull(clock);
        this.state = Objects.requireNonNull(state);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        Instant start = Instant.now(clock);
        try {
            result = method.invoke(delegate, args);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected invocation exception: " +
                    e.getMessage());
        } finally {
            if (method.isAnnotationPresent(Profiled.class)) {
                Duration duration = Duration.between(start, Instant.now(clock));
                state.record(delegate.getClass(), method, duration);
            }
        }
        return result;
    }
}
