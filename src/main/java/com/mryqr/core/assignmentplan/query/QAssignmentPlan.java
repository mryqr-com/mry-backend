package com.mryqr.core.assignmentplan.query;

import com.mryqr.core.assignmentplan.domain.AssignmentSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QAssignmentPlan {
    private final String id;
    private final String name;
    private final AssignmentSetting setting;
    private final Instant createdAt;
    private final String creator;
    private List<String> excludedGroups;
    private List<String> operators;
    private List<String> operatorNames;
    private Instant nextAssignmentStartAt;
    private boolean active;
}
