package com.mryqr.core.assignmentplan.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QAssignmentPlanSummary {
    private final String id;
    private final String name;
}
