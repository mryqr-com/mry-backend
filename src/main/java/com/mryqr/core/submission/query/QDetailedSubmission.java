package com.mryqr.core.submission.query;

import com.mryqr.core.submission.domain.answer.Answer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QDetailedSubmission {
    private final String id;
    private final String tenantId;
    private final String qrId;
    private final String groupId;
    private final String appId;
    private final String pageId;
    private final Set<Answer> answers;
    private final QSubmissionApproval approval;
    private final Instant createdAt;
    private final String createdBy;
    private final String creatorName;
    private final boolean canUpdate;
    private final boolean canApprove;
    private final String referenceData;
}
