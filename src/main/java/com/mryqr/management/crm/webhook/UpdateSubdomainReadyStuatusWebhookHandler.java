package com.mryqr.management.crm.webhook;

import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionCreatedWebhookPayload;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.radio.RadioAnswer;
import com.mryqr.core.tenant.command.TenantCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.mryqr.common.domain.user.User.NOUSER;
import static com.mryqr.management.crm.MryTenantManageApp.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateSubdomainReadyStuatusWebhookHandler implements TenantWebhookHandler {
    private final TenantCommandService tenantCommandService;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload instanceof SubmissionCreatedWebhookPayload && payload.getPageId().equals(UPDATE_SUBDOMAIN_READY_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String tenantId = payload.getQrCustomId();
        Map<String, Answer> answers = payload.allAnswers();

        RadioAnswer readyStatusAnswer = (RadioAnswer) answers.get(UPDATE_SUBDOMAIN_READY_CONTROL_ID);

        if (readyStatusAnswer != null) {
            String optionId = readyStatusAnswer.getOptionId();
            boolean isReady = Objects.equals(optionId, SUBDOMAIN_READY_OPTION_ID);
            tenantCommandService.updateSubdomainReadyStatus(tenantId, isReady, NOUSER);
            log.info("Update subdomain ready status to {} for tenant[{}].", isReady, tenantId);
        }
    }
}
