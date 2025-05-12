package com.mryqr.management.crm.webhook;

import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionCreatedWebhookPayload;
import com.mryqr.core.tenant.domain.task.SyncTenantToManagedQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import static com.mryqr.management.crm.MryTenantManageApp.TRIGGER_SYNC_PAGE_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantSyncWebhookHandler implements TenantWebhookHandler {
    private final TaskExecutor taskExecutor;
    private final SyncTenantToManagedQrTask syncTenantToManagedQrTask;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload instanceof SubmissionCreatedWebhookPayload && payload.getPageId().equals(TRIGGER_SYNC_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String tenantId = payload.getQrCustomId();
        taskExecutor.execute(() -> syncTenantToManagedQrTask.sync(tenantId));
    }
}
