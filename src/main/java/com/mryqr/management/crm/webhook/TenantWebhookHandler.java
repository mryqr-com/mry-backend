package com.mryqr.management.crm.webhook;

import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;

public interface TenantWebhookHandler {
    boolean canHandle(BaseSubmissionWebhookPayload payload);

    void handle(BaseSubmissionWebhookPayload payload);
}
