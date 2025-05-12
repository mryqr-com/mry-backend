package com.mryqr.management.order.webhook;

import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;

public interface OrderWebhookHandler {
    boolean canHandle(BaseSubmissionWebhookPayload payload);

    void handle(BaseSubmissionWebhookPayload payload);
}
