package com.mryqr.management.order.webhook;

import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.core.order.command.OrderCommandService;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
import com.mryqr.core.submission.domain.answer.identifier.IdentifierAnswer;
import com.mryqr.core.submission.domain.answer.time.TimeAnswer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import static com.mryqr.common.domain.user.User.NO_USER;
import static com.mryqr.management.order.MryOrderManageApp.*;
import static java.time.ZoneId.systemDefault;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderWxPayWebhookHandler implements OrderWebhookHandler {
    private final OrderCommandService orderCommandService;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload.getPageId().equals(ORDER_REGISTER_WX_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String orderId = payload.getQrCustomId();
        Map<String, Answer> answers = payload.allAnswers();

        String wxTxnId = null;
        IdentifierAnswer identifierAnswer = (IdentifierAnswer) answers.get(ORDER_REGISTER_WX_TXN_CONTROL_ID);
        if (identifierAnswer != null) {
            wxTxnId = identifierAnswer.getContent();
        }

        String transferDate = "2000-01-01";
        DateAnswer dateAnswer = (DateAnswer) answers.get(ORDER_REGISTER_WX_DATE_CONTROL_ID);
        if (dateAnswer != null) {
            transferDate = dateAnswer.getDate();
        }

        String transferTime = "00:00";
        TimeAnswer timeAnswer = (TimeAnswer) answers.get(ORDER_REGISTER_WX_TIME_CONTROL_ID);
        if (timeAnswer != null) {
            transferTime = timeAnswer.getTime();
        }

        LocalDate localDate = LocalDate.parse(transferDate);
        LocalTime localTime = LocalTime.parse(transferTime);
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        Instant paidAt = localDateTime.atZone(systemDefault()).toInstant();

        orderCommandService.wxPay(orderId, wxTxnId, paidAt, NO_USER);
        log.info("Updated wx pay info for order[{}].", orderId);
    }
}
