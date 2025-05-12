package com.mryqr.common.webhook.submission;

import com.mryqr.common.webhook.WebhookPayload;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.mryqr.common.webhook.WebhookPayloadType.SUBMISSION_DELETED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class SubmissionDeletedWebhookPayload extends WebhookPayload {
    private String submissionId;
    private String qrId;
    private String plateId;
    private String pageId;

    public SubmissionDeletedWebhookPayload(String submissionId,
                                           String qrId,
                                           String plateId,
                                           String appId,
                                           String pageId,
                                           String eventId) {
        super(SUBMISSION_DELETED, appId, eventId);
        this.submissionId = submissionId;
        this.qrId = qrId;
        this.plateId = plateId;
        this.pageId = pageId;
    }
}
