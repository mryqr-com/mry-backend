package com.mryqr.core.assignment.query;


import com.mryqr.core.assignment.domain.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QAssignmentQrDetail {
    private final String assignmentId;
    private final AssignmentStatus status;
    private final int allQrCount;
    private final int finishedQrCount;

    private final String qrId;
    private final boolean finished;
    private final String submissionId;
    private final String operatorId;
    private final String operatorName;
    private final Instant finishedAt;
}
