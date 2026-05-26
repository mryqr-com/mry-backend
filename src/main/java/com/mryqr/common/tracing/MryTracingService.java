package com.mryqr.common.tracing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MryTracingService {

    public String currentTraceId() {
        return null; // todo: impl
    }

}
