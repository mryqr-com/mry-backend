package com.mryqr.core.assignment.query;

import com.mryqr.core.assignment.domain.AssignmentStatus;
import com.mryqr.core.common.utils.Query;
import com.mryqr.core.common.validation.id.app.AppId;
import com.mryqr.core.common.validation.id.group.GroupId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ListMyAssignmentsQuery implements Query {
    @AppId
    @NotBlank
    private final String appId;

    @GroupId
    private final String groupId;

    private final AssignmentStatus status;

    @Min(1)
    private final int pageIndex;

    @Min(10)
    @Max(100)
    private final int pageSize;

    @Size(max = 50)
    private final String sortedBy;

    private final boolean ascSort;
}
