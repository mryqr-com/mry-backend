package com.mryqr.management.crm.webhook;

import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionCreatedWebhookPayload;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.tenant.command.TenantCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.mryqr.core.common.domain.user.User.NOUSER;
import static com.mryqr.management.crm.MryTenantManageApp.CLEAR_SUBDOMAIN_NOTE_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.CLEAR_SUBDOMAIN_PAGE_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClearSubdomainWebhookHandler implements TenantWebhookHandler {
    private final TenantCommandService tenantCommandService;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload instanceof SubmissionCreatedWebhookPayload && payload.getPageId().equals(CLEAR_SUBDOMAIN_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String tenantId = payload.getQrCustomId();
        Map<String, Answer> answers = payload.allAnswers();

        SingleLineTextAnswer noteAnswer = (SingleLineTextAnswer) answers.get(CLEAR_SUBDOMAIN_NOTE_CONTROL_ID);
        String note = null;
        if (noteAnswer != null) {
            note = noteAnswer.getContent();
        }

        tenantCommandService.clearSubdomain(tenantId, NOUSER);
        log.info("Cleared subdomain for tenant[{}] with note[{}].", tenantId, note);
    }
}
