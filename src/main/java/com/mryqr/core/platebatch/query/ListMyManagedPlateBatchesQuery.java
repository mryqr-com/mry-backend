package com.mryqr.core.platebatch.query;

import com.mryqr.core.common.utils.Query;
import com.mryqr.core.common.validation.id.app.AppId;
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
public class ListMyManagedPlateBatchesQuery implements Query {
    @AppId
    @NotBlank
    private final String appId;

    @Size(max = 50)
    private final String search;

    @Min(1)
    private final int pageIndex;

    @Min(10)
    @Max(100)
    private final int pageSize;

    @Size(max = 50)
    private final String sortedBy;

    private final boolean ascSort;
}
