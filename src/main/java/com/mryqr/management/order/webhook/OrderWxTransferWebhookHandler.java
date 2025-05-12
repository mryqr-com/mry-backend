package com.mryqr.management.order.webhook;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.core.order.command.OrderCommandService;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
import com.mryqr.core.submission.domain.answer.fileupload.FileUploadAnswer;
import com.mryqr.core.submission.domain.answer.time.TimeAnswer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static com.mryqr.common.domain.user.User.NOUSER;
import static com.mryqr.management.order.MryOrderManageApp.*;
import static java.time.ZoneId.systemDefault;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderWxTransferWebhookHandler implements OrderWebhookHandler {
    private final OrderCommandService orderCommandService;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload.getPageId().equals(ORDER_REGISTER_WX_TRANSFER_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String orderId = payload.getQrCustomId();
        Map<String, Answer> answers = payload.allAnswers();

        List<UploadedFile> screenShots = List.of();
        FileUploadAnswer screenShotsAnswer = (FileUploadAnswer) answers.get(ORDER_REGISTER_WX_TRANSFER_SCREENSHOTS_CONTROL_ID);
        if (screenShotsAnswer != null) {
            screenShots = screenShotsAnswer.getFiles();
        }

        String transferDate = "2000-01-01";
        DateAnswer dateAnswer = (DateAnswer) answers.get(ORDER_REGISTER_WX_TRANSFER_DATE_CONTROL_ID);
        if (dateAnswer != null) {
            transferDate = dateAnswer.getDate();
        }

        String transferTime = "00:00";
        TimeAnswer timeAnswer = (TimeAnswer) answers.get(ORDER_REGISTER_WX_TRANSFER_TIME_CONTROL_ID);
        if (timeAnswer != null) {
            transferTime = timeAnswer.getTime();
        }

        LocalDate localDate = LocalDate.parse(transferDate);
        LocalTime localTime = LocalTime.parse(transferTime);
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        Instant paidAt = localDateTime.atZone(systemDefault()).toInstant();

        orderCommandService.wxTransferPay(orderId, screenShots, paidAt, NOUSER);
        log.info("Updated wx transfer payment info for order[{}].", orderId);
    }
}
