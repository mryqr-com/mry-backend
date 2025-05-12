package com.mryqr.core.submission.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class SubmissionApproval {
    private final boolean passed;
    private final String note;
    private final Instant approvedAt;
    private final String approvedBy;
}
