package com.mryqr.core.app.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QAppResourceUsages {
    private final int usedQrCount;
    private final int usedGroupCount;
    private final int usedSubmissionCount;
}
