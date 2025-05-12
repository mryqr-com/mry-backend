package com.mryqr.core.assignment.query;


import com.mryqr.core.assignment.domain.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QListAssignment {
    private final String id;
    private final String assignmentPlanId;
    private final String name;
    private final String groupId;
    private final Instant startAt;
    private final Instant expireAt;
    private final List<String> operators;
    private final List<String> operatorNames;
    private final AssignmentStatus status;
    private final Instant createdAt;
    private final int allQrCount;
    private final int finishedQrCount;
}
