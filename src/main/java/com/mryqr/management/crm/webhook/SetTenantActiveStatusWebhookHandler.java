package com.mryqr.management.crm.webhook;

import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionCreatedWebhookPayload;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.radio.RadioAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.tenant.command.TenantCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.mryqr.common.domain.user.User.NO_USER;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_TENANT_ID;
import static com.mryqr.management.crm.MryTenantManageApp.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetTenantActiveStatusWebhookHandler implements TenantWebhookHandler {
    private final TenantCommandService tenantCommandService;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload instanceof SubmissionCreatedWebhookPayload && payload.getPageId().equals(STATUS_SETTING_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String tenantId = payload.getQrCustomId();

        if (Objects.equals(tenantId, MRY_MANAGE_TENANT_ID)) {
            log.warn("Cannot set active status for Mry management tenant[{}].", tenantId);
            return;
        }

        Map<String, Answer> answers = payload.allAnswers();

        SingleLineTextAnswer noteAnswer = (SingleLineTextAnswer) answers.get(STATUS_SETTING_NOTE_CONTROL_ID);
        String note = null;
        if (noteAnswer != null) {
            note = noteAnswer.getContent();
        }

        RadioAnswer statusAnswer = (RadioAnswer) answers.get(STATUS_SETTING_CONTROL_ID);
        if (statusAnswer != null) {
            String optionId = statusAnswer.getOptionId();
            if (Objects.equals(optionId, STATUS_SETTING_ACTIVE_OPTION_ID)) {
                tenantCommandService.activateTenant(tenantId, NO_USER);
                log.info("Activated tenant[{}] with note[{}].", tenantId, note);
            } else if (Objects.equals(optionId, STATUS_SETTING_INACTIVE_OPTION_ID)) {
                tenantCommandService.deactivateTenant(tenantId, NO_USER);
                log.info("Deactivated tenant[{}] with note[{}].", tenantId, note);
            }
        }
    }
}
