package com.mryqr.core.report.query.number;

import com.mryqr.core.app.domain.report.number.NumberReport;
import com.mryqr.core.common.utils.Query;
import com.mryqr.core.common.validation.id.app.AppId;
import com.mryqr.core.common.validation.id.group.GroupId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class NumberReportQuery implements Query {

    @AppId
    @NotBlank
    private final String appId;

    @GroupId
    private final String groupId;

    @Valid
    @NotNull
    private final NumberReport report;
}
