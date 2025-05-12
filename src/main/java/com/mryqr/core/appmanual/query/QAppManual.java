package com.mryqr.core.appmanual.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QAppManual {
    private final String id;
    private final String appId;
    private final String content;
}
