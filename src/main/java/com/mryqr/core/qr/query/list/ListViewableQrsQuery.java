package com.mryqr.core.qr.query.list;

import com.mryqr.core.common.domain.Geopoint;
import com.mryqr.core.common.utils.Query;
import com.mryqr.core.common.validation.collection.NoBlankString;
import com.mryqr.core.common.validation.id.app.AppId;
import com.mryqr.core.common.validation.id.group.GroupId;
import com.mryqr.core.common.validation.id.member.MemberId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Set;

import static com.mryqr.core.common.utils.MryRegexConstants.DATE_PATTERN;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ListViewableQrsQuery implements Query {

    @AppId
    @NotBlank
    private final String appId;

    @GroupId
    private final String groupId;

    @Size(max = 50)
    private final String search;

    @Min(1)
    private final int pageIndex;

    @Min(10)
    @Max(100)
    private final int pageSize;

    @Size(max = 20)
    private final Map<@NotBlank String, @NotNull @NoBlankString @Size(max = 20) Set<@Size(max = 200) String>> filterables;

    @Size(max = 50)
    private final String sortedBy;

    private final boolean ascSort;

    private final boolean templateOnly;

    private final boolean inactiveOnly;

    private final boolean nearestPointEnabled;

    @Valid
    private final Geopoint currentPoint;

    @MemberId
    private final String createdBy;

    @Size(max = 50)
    @Pattern(regexp = DATE_PATTERN, message = "创建开始日期格式不正确。")
    private final String startDate;

    @Size(max = 50)
    @Pattern(regexp = DATE_PATTERN, message = "创建结束日期格式不正确。")
    private final String endDate;
}
