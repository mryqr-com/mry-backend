package com.mryqr.core.member.query;

import com.mryqr.core.common.utils.Query;
import com.mryqr.core.common.validation.id.department.DepartmentId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.core.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ListMyManagedMembersQuery implements Query {

    @DepartmentId
    private final String departmentId;

    @Size(max = 50)
    private final String search;

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String sortedBy;

    private final boolean ascSort;

    @Min(1)
    private final int pageIndex;

    @Min(10)
    @Max(100)
    private final int pageSize;

}
