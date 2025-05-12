package com.mryqr.management.order.webhook;

import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.core.order.command.OrderCommandService;
import com.mryqr.core.qr.command.QrCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.common.domain.user.User.robotUser;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_DELETE_PAGE_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDeleteWebhookHandler implements OrderWebhookHandler {
    private final OrderCommandService orderCommandService;
    private final QrCommandService qrCommandService;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload.getPageId().equals(ORDER_DELETE_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String orderId = payload.getQrCustomId();
        String qrId = payload.getQrId();

        qrCommandService.deleteQr(qrId, robotUser(payload.getTenantId()));
        orderCommandService.delete(orderId);
        log.info("Deleted order[{}].", orderId);
    }
}
