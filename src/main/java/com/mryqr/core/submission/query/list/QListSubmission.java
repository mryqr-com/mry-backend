package com.mryqr.core.submission.query.list;

import com.mryqr.common.domain.display.DisplayValue;
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
public class QListSubmission {
    private final String id;
    private final String plateId;
    private final String qrId;
    private final String qrName;
    private final String appId;
    private final String groupId;
    private final String pageId;
    private final Map<String, DisplayValue> displayAnswers;
    private final ApprovalStatus approvalStatus;
    private final String createdBy;
    private final String creator;
    private final Instant createdAt;
    private final String referenceData;
}
