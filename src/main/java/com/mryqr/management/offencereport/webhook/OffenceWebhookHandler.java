package com.mryqr.management.offencereport.webhook;

import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;

public interface OffenceWebhookHandler {
    boolean canHandle(BaseSubmissionWebhookPayload payload);

    void handle(BaseSubmissionWebhookPayload payload);
}
