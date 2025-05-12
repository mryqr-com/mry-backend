package com.mryqr.common.webhook.submission;

import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.Submission;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.mryqr.common.webhook.WebhookPayloadType.SUBMISSION_CREATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class SubmissionCreatedWebhookPayload extends BaseSubmissionWebhookPayload {
    public SubmissionCreatedWebhookPayload(Submission submission, QR qr, String eventId) {
        super(SUBMISSION_CREATED, submission, qr, eventId);
    }
}
