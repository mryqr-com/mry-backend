package com.mryqr.core.assignment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class AssignmentFinishedQr {
    private final String qrId;
    private final String submissionId;
    private final Instant finishedAt;
    private final String operatorId;
}
