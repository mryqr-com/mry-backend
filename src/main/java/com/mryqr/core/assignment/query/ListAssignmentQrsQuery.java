package com.mryqr.core.assignment.query;

import com.mryqr.core.common.domain.Geopoint;
import com.mryqr.core.common.utils.Query;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ListAssignmentQrsQuery implements Query {

    private final Boolean finished;

    @Size(max = 50)
    private final String search;

    private final boolean nearestPointEnabled;

    @Valid
    private final Geopoint currentPoint;

    @Min(1)
    private final int pageIndex;

    @Min(10)
    @Max(100)
    private final int pageSize;

    @Size(max = 50)
    private final String sortedBy;

    private final boolean ascSort;
}
