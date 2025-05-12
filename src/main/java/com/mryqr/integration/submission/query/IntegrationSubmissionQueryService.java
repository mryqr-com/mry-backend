package com.mryqr.integration.submission.query;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntegrationSubmissionQueryService {
    private final SubmissionRepository submissionRepository;
    private final MryRateLimiter mryRateLimiter;

    public QIntegrationSubmission fetchSubmission(String submissionId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Submission:Fetch", 10);

        Submission submission = submissionRepository.byIdAndCheckTenantShip(submissionId, user);

        return QIntegrationSubmission.builder()
                .id(submission.getId())
                .qrId(submission.getQrId())
                .plateId(submission.getPlateId())
                .groupId(submission.getGroupId())
                .appId(submission.getAppId())
                .pageId(submission.getPageId())
                .answers(submission.getAnswers())
                .referenceData(submission.getReferenceData())
                .approval(submission.getApproval())
                .updatedAt(submission.getUpdatedAt())
                .updatedBy(submission.getUpdatedBy())
                .createdAt(submission.getCreatedAt())
                .createdBy(submission.getCreatedBy())
                .build();
    }
}
