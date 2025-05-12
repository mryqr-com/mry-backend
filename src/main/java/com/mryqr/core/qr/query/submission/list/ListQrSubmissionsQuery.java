package com.mryqr.core.qr.query.submission.list;

import com.mryqr.common.utils.Query;
import com.mryqr.common.validation.collection.NoBlankString;
import com.mryqr.common.validation.id.member.MemberId;
import com.mryqr.common.validation.id.page.PageId;
import com.mryqr.core.app.domain.operationmenu.SubmissionListType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Set;

import static com.mryqr.common.utils.MryRegexConstants.DATE_PATTERN;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ListQrSubmissionsQuery implements Query {

    @NotNull
    private final SubmissionListType type;

    @PageId
    private final String pageId;

    @MemberId
    private final String createdBy;

    @Size(max = 20)
    private final Map<@NotBlank String, @NotNull @NoBlankString @Size(max = 20) Set<@Size(max = 200) String>> filterables;

    @Size(max = 50)
    private final String sortedBy;

    private final boolean ascSort;

    @Min(1)
    private final int pageIndex;

    @Min(10)
    @Max(100)
    private final int pageSize;

    @Size(max = 50)
    private final String search;

    @Size(max = 50)
    @Pattern(regexp = DATE_PATTERN, message = "提交开始日期格式不正确。")
    private final String startDate;

    @Size(max = 50)
    @Pattern(regexp = DATE_PATTERN, message = "提交结束日期格式不正确。")
    private final String endDate;
}
