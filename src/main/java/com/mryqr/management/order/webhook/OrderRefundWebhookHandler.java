package com.mryqr.management.order.webhook;

import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.core.order.command.OrderCommandService;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.mryqr.common.domain.user.User.NO_USER;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_REFUND_PAGE_ID;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_REFUND_REASON_CONTROL_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRefundWebhookHandler implements OrderWebhookHandler {
    private final OrderCommandService orderCommandService;


    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload.getPageId().equals(ORDER_REFUND_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String orderId = payload.getQrCustomId();
        Map<String, Answer> answers = payload.allAnswers();

        String reason = null;
        SingleLineTextAnswer reasonAnswer = (SingleLineTextAnswer) answers.get(ORDER_REFUND_REASON_CONTROL_ID);
        if (reasonAnswer != null) {
            reason = reasonAnswer.getContent();
        }

        orderCommandService.refund(orderId, reason, NO_USER);
        log.info("Updated refund info for order[{}] .", orderId);
    }
}
