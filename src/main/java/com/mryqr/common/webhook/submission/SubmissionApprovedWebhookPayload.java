package com.mryqr.common.webhook.submission;

import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionApproval;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.mryqr.common.webhook.WebhookPayloadType.SUBMISSION_APPROVED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@NoArgsConstructor(access = PRIVATE)
public class SubmissionApprovedWebhookPayload extends BaseSubmissionWebhookPayload {
    private SubmissionApproval approval;

    public SubmissionApprovedWebhookPayload(Submission submission, QR qr, String eventId) {
        super(SUBMISSION_APPROVED, submission, qr, eventId);
        this.approval = submission.getApproval();
    }
}
