package com.mryqr.common.webhook.submission;

import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.Submission;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static com.mryqr.common.webhook.WebhookPayloadType.SUBMISSION_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class SubmissionUpdatedWebhookPayload extends BaseSubmissionWebhookPayload {
    private Instant updatedAt;
    private String updatedBy;

    public SubmissionUpdatedWebhookPayload(Submission submission, QR qr, Instant updatedAt, String updatedBy, String eventId) {
        super(SUBMISSION_UPDATED, submission, qr, eventId);
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }
}
