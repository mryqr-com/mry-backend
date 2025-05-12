package com.mryqr.management.crm.webhook;

import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionCreatedWebhookPayload;
import com.mryqr.core.plan.domain.Plan;
import com.mryqr.core.plan.domain.PlanType;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
import com.mryqr.core.submission.domain.answer.dropdown.DropdownAnswer;
import com.mryqr.core.submission.domain.answer.numberinput.NumberInputAnswer;
import com.mryqr.core.submission.domain.answer.radio.RadioAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.tenant.command.TenantCommandService;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

import static com.mryqr.core.common.domain.user.User.NOUSER;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_TENANT_ID;
import static com.mryqr.management.common.PlanTypeControl.OPTION_TO_PLAN_MAP;
import static com.mryqr.management.crm.MryTenantManageApp.*;
import static java.time.ZoneId.systemDefault;
import static org.apache.commons.collections4.MapUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetTenantFeatureLimitWebhookHandler implements TenantWebhookHandler {
    private final TenantCommandService tenantCommandService;
    private final TenantRepository tenantRepository;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload instanceof SubmissionCreatedWebhookPayload && payload.getPageId().equals(LIMIT_SETTING_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String tenantId = payload.getQrCustomId();

        if (Objects.equals(tenantId, MRY_MANAGE_TENANT_ID)) {
            log.warn("Cannot set packages for Mry management tenant[{}].", tenantId);
            return;
        }

        Map<String, Answer> answers = payload.allAnswers();
        if (isEmpty(answers)) {
            log.warn("No feature limit provided for Mry management tenant[{}] when trying to update plan limit.", tenantId);
            return;
        }


        Plan.PlanBuilder planBuilder = copyOfExistingPlanBuilder(tenantId);

        NumberInputAnswer appCountAnswer = (NumberInputAnswer) answers.get(LIMIT_SETTING_APP_COUNT_CONTROL_ID);
        if (appCountAnswer != null) {
            planBuilder.maxAppCount(appCountAnswer.getNumber().intValue());
        }

        NumberInputAnswer qrCountAnswer = (NumberInputAnswer) answers.get(LIMIT_SETTING_QR_COUNT_CONTROL_ID);
        if (qrCountAnswer != null) {
            planBuilder.maxQrCount(qrCountAnswer.getNumber().intValue());
        }

        NumberInputAnswer submissionCountAnswer = (NumberInputAnswer) answers.get(LIMIT_SETTING_SUBMISSION_COUNT_CONTROL_ID);
        if (submissionCountAnswer != null) {
            planBuilder.maxSubmissionCount(submissionCountAnswer.getNumber().intValue());
        }

        NumberInputAnswer memberCountAnswer = (NumberInputAnswer) answers.get(LIMIT_SETTING_MEMBER_COUNT_CONTROL_ID);
        if (memberCountAnswer != null) {
            planBuilder.maxMemberCount(memberCountAnswer.getNumber().intValue());
        }

        NumberInputAnswer storageCountAnswer = (NumberInputAnswer) answers.get(LIMIT_SETTING_STORAGE_COUNT_CONTROL_ID);
        if (storageCountAnswer != null) {
            planBuilder.maxStorage(storageCountAnswer.getNumber().intValue());
        }

        NumberInputAnswer smsCountAnswer = (NumberInputAnswer) answers.get(LIMIT_SETTING_SMS_COUNT_CONTROL_ID);
        if (smsCountAnswer != null) {
            planBuilder.maxSmsCountPerMonth(smsCountAnswer.getNumber().intValue());
        }

        NumberInputAnswer groupCountAnswer = (NumberInputAnswer) answers.get(LIMIT_SETTING_GROUP_PER_APP_COUNT_CONTROL_ID);
        if (groupCountAnswer != null) {
            planBuilder.maxGroupCountPerApp(groupCountAnswer.getNumber().intValue());
        }

        NumberInputAnswer departmentCountAnswer = (NumberInputAnswer) answers.get(LIMIT_SETTING_DEPARTMENT_COUNT_CONTROL_ID);
        if (departmentCountAnswer != null) {
            planBuilder.maxDepartmentCount(departmentCountAnswer.getNumber().intValue());
        }

        RadioAnswer developerEnabledAnswer = (RadioAnswer) answers.get(LIMIT_SETTING_DEVELOPER_CONTROL_ID);
        if (developerEnabledAnswer != null) {
            planBuilder.developerAllowed(Objects.equals(developerEnabledAnswer.getOptionId(), LIMIT_SETTING_DEVELOPER_ACTIVATED_ID));
        }

        Plan finalPlan = planBuilder.build();
        tenantCommandService.updateTenantPlan(tenantId, finalPlan, NOUSER);
        log.info("Updated plan limit for tenant[{}].", tenantId);
    }

    private Plan.PlanBuilder copyOfExistingPlanBuilder(String tenantId) {
        Tenant tenant = tenantRepository.byId(tenantId);
        Plan sourcePlan = tenant.currentPlan();
        return Plan.builder()
                .type(sourcePlan.getType())
                .maxAppCount(sourcePlan.getMaxAppCount())
                .maxQrCount(sourcePlan.getMaxQrCount())
                .maxSubmissionCount(sourcePlan.getMaxSubmissionCount())
                .maxMemberCount(sourcePlan.getMaxMemberCount())
                .maxStorage(sourcePlan.getMaxStorage())
                .maxSmsCountPerMonth(sourcePlan.getMaxSmsCountPerMonth())
                .maxDepartmentCount(sourcePlan.getMaxDepartmentCount())
                .maxGroupCountPerApp(sourcePlan.getMaxGroupCountPerApp())
                .maxVideoTrafficPerMonth(sourcePlan.getMaxVideoTrafficPerMonth())
                .supportedControlTypes(sourcePlan.getSupportedControlTypes())
                .customSubdomainAllowed(sourcePlan.isCustomSubdomainAllowed())
                .customLogoAllowed(sourcePlan.isCustomLogoAllowed())
                .hideBottomMryLogo(sourcePlan.isHideBottomMryLogo())
                .hideAds(sourcePlan.isHideAds())
                .videoAudioAllowed(sourcePlan.isVideoAudioAllowed())
                .developerAllowed(sourcePlan.isDeveloperAllowed())
                .reportingAllowed(sourcePlan.isReportingAllowed())
                .kanbanAllowed(sourcePlan.isKanbanAllowed())
                .submissionNotifyAllowed(sourcePlan.isSubmissionNotifyAllowed())
                .batchImportQrAllowed(sourcePlan.isBatchImportQrAllowed())
                .batchImportMemberAllowed(sourcePlan.isBatchImportMemberAllowed())
                .submissionApprovalAllowed(sourcePlan.isSubmissionApprovalAllowed())
                .assignmentAllowed(sourcePlan.isAssignmentAllowed());
    }
}
