package com.mryqr.common.tracing;

import io.micrometer.tracing.ScopedSpan;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MryTracingService {
    private final Tracer tracer;

    public String currentTraceId() {
        Span span = tracer.currentSpan();
        return span != null ? span.context().traceId() : null;
    }

    public ScopedSpan startNewSpan(String name) {
        return tracer.startScopedSpan(name);
    }

}
