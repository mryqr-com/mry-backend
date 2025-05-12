package com.mryqr.core.submission.query.autocalculate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ItemStatusAutoCalculateResponse {
    private final String optionId;
}
