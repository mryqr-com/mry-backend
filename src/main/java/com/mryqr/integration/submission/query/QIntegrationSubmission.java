package com.mryqr.integration.submission.query;

import com.mryqr.core.submission.domain.SubmissionApproval;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QIntegrationSubmission {
    private final String id;
    private final String qrId;
    private final String plateId;
    private final String groupId;
    private final String appId;
    private final String pageId;
    private final Map<String, Answer> answers;
    private final String referenceData;
    private final SubmissionApproval approval;
    private final Instant createdAt;
    private final String createdBy;
    private final Instant updatedAt;
    private final String updatedBy;
}
