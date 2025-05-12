package com.mryqr.common.webhook.submission;

import com.mryqr.common.webhook.WebhookPayload;
import com.mryqr.common.webhook.WebhookPayloadType;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public abstract class BaseSubmissionWebhookPayload extends WebhookPayload {
    private String submissionId;
    private String qrId;
    private String plateId;
    private String qrCustomId;
    private String groupId;
    private String pageId;
    private List<Answer> answers;
    private String tenantId;
    private Instant createdAt;
    private String createdBy;
    private String referenceData;

    protected BaseSubmissionWebhookPayload(WebhookPayloadType type, Submission submission, QR qr, String eventId) {
        super(type, submission.getAppId(), eventId);
        this.submissionId = submission.getId();
        this.qrId = submission.getQrId();
        this.plateId = submission.getPlateId();
        this.qrCustomId = qr.getCustomId();
        this.groupId = submission.getGroupId();
        this.pageId = submission.getPageId();
        this.answers = submission.getAnswers().values().stream().collect(toImmutableList());
        this.tenantId = submission.getTenantId();
        this.createdAt = submission.getCreatedAt();
        this.createdBy = submission.getCreatedBy();
        this.referenceData = submission.getReferenceData();
    }

    public Map<String, Answer> allAnswers() {
        return this.answers.stream().collect(toImmutableMap(Answer::getControlId, identity()));
    }
}
