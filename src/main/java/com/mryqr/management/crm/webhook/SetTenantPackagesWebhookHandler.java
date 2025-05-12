package com.mryqr.management.crm.webhook;

import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionCreatedWebhookPayload;
import com.mryqr.core.plan.domain.PlanType;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
import com.mryqr.core.submission.domain.answer.dropdown.DropdownAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.tenant.command.TenantCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

import static com.mryqr.core.common.domain.user.User.NOUSER;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_TENANT_ID;
import static com.mryqr.management.common.PlanTypeControl.OPTION_TO_PLAN_MAP;
import static com.mryqr.management.crm.MryTenantManageApp.PACKAGE_SETTING_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.PACKAGE_SETTING_EXPIRE_DATE_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.PACKAGE_SETTING_NOTE_CONTROL_ID;
import static com.mryqr.management.crm.MryTenantManageApp.PACKAGE_SETTING_PAGE_ID;
import static java.time.ZoneId.systemDefault;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetTenantPackagesWebhookHandler implements TenantWebhookHandler {
    private final TenantCommandService tenantCommandService;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload instanceof SubmissionCreatedWebhookPayload && payload.getPageId().equals(PACKAGE_SETTING_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String tenantId = payload.getQrCustomId();

        if (Objects.equals(tenantId, MRY_MANAGE_TENANT_ID)) {
            log.warn("Cannot set packages for Mry management tenant[{}].", tenantId);
            return;
        }

        Map<String, Answer> answers = payload.allAnswers();

        SingleLineTextAnswer noteAnswer = (SingleLineTextAnswer) answers.get(PACKAGE_SETTING_NOTE_CONTROL_ID);
        String note = null;
        if (noteAnswer != null) {
            note = noteAnswer.getContent();
        }

        PlanType planType = null;
        DropdownAnswer packagesAnswer = (DropdownAnswer) answers.get(PACKAGE_SETTING_CONTROL_ID);
        if (packagesAnswer != null) {
            String optionId = packagesAnswer.getOptionIds().get(0);
            planType = OPTION_TO_PLAN_MAP.get(optionId);
        }

        String expireDate = null;
        DateAnswer dateAnswer = (DateAnswer) answers.get(PACKAGE_SETTING_EXPIRE_DATE_CONTROL_ID);
        if (dateAnswer != null) {
            expireDate = dateAnswer.getDate();
        }

        if (planType != null && expireDate != null) {
            LocalDate localDate = LocalDate.parse(expireDate);
            Instant expire = localDate.atStartOfDay(systemDefault()).toInstant();
            tenantCommandService.updateTenantPlanType(tenantId, planType, expire, NOUSER);
            log.info("Set tenant[{}] packages to {} and expire at {} with note[{}].", tenantId, planType, expire, note);
        }
    }
}
