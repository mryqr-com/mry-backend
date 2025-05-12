package com.mryqr.management.order.webhook;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.core.order.command.OrderCommandService;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.fileupload.FileUploadAnswer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.mryqr.common.domain.user.User.NO_USER;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_REGISTER_INVOICE_FILE_CONTROL_ID;
import static com.mryqr.management.order.MryOrderManageApp.ORDER_REGISTER_INVOICE_PAGE_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderInvoiceWebhookHandler implements OrderWebhookHandler {
    private final OrderCommandService orderCommandService;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload.getPageId().equals(ORDER_REGISTER_INVOICE_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String orderId = payload.getQrCustomId();
        Map<String, Answer> answers = payload.allAnswers();

        List<UploadedFile> invoiceFiles = List.of();
        FileUploadAnswer invoiceFileAnswer = (FileUploadAnswer) answers.get(ORDER_REGISTER_INVOICE_FILE_CONTROL_ID);
        if (invoiceFileAnswer != null) {
            invoiceFiles = invoiceFileAnswer.getFiles();
        }

        orderCommandService.issueInvoice(orderId, invoiceFiles, NO_USER);
        log.info("Issued invoice for order[{}].", orderId);
    }
}
