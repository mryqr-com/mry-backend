package com.mryqr.core.presentation.query.submithistory;

import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.submission.domain.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QSubmitHistorySubmission {
    private String id;
    private String pageId;
    private Map<String, DisplayValue> values;
    private ApprovalStatus approvalStatus;
    private String createdBy;
    private String creator;
    private Instant createdAt;
}
