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
public class QAssignmentDetail {
    private final String id;
    private final String name;
    private final List<String> operatorNames;
    private final String groupId;
    private final String pageId;
    private final Instant startAt;
    private final Instant expireAt;
    private final int allQrCount;
    private final int finishedQrCount;
    private final AssignmentStatus status;
}
