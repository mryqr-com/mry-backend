package com.mryqr.management.order.webhook;

import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.core.order.domain.task.SyncOrderToManagedQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.mryqr.management.order.MryOrderManageApp.ORDER_TRIGGER_SYNC_PAGE_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSyncWebhookHandler implements OrderWebhookHandler {
    private final TaskExecutor taskExecutor;
    private final SyncOrderToManagedQrTask syncOrderToManagedQrTask;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload.getPageId().equals(ORDER_TRIGGER_SYNC_PAGE_ID);
    }

    @Override
    @Transactional
    public void handle(BaseSubmissionWebhookPayload payload) {
        String orderId = payload.getQrCustomId();
        taskExecutor.execute(() -> syncOrderToManagedQrTask.sync(orderId));
    }
}
