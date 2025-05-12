package com.mryqr.core.tenant.domain;

import com.mryqr.common.exception.MryException;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.plan.domain.PlanType;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.DropdownAttributeValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_TENANT_ID;
import static com.mryqr.management.apptemplate.MryAppTemplateManageApp.TEMPLATE_PLAN_TYPE_ATTRIBUTE_ID;
import static com.mryqr.management.apptemplate.MryAppTemplateTenant.MRY_APP_TEMPLATE_TENANT_ID;
import static com.mryqr.management.common.PlanTypeControl.OPTION_TO_PLAN_MAP;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public final class PackagesStatus {
    public static final int MAX_PLATE_SIZE = 100000000;
    private final String id;//租户ID
    private final Packages packages;//租户当前套餐
    private final ResourceUsage resourceUsage;//租户当前的资源使用量

    public void validateAddMember() {
        if (isMaxMemberReached()) {
            if (isExpired()) {
                throw new MryException(MEMBER_COUNT_LIMIT_REACHED,
                        "当前套餐(" + currentPlanName() + ")已过期，且成员总数已达免费版上限，无法继续添加成员，如需添加请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(MEMBER_COUNT_LIMIT_REACHED,
                    "无法继续添加成员，成员总数已经达到当前套餐(" + currentPlanName() + ")上限，如需添加请及时升级或增购成员数量。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public void validateImportMember() {
        if (!this.packages.batchImportMemberAllowed()) {
            if (isExpired()) {
                throw new MryException(BATCH_MEMBER_IMPORT_NOT_ALLOWED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，无法使用批量导入功能，请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(BATCH_MEMBER_IMPORT_NOT_ALLOWED,
                    "当前套餐(" + currentPlanName() + ")无法使用批量导入功能，请及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public int validateImportMembers() {
        int maxAllowedMemberCount = packages.effectiveMaxMemberCount();
        int currentMemberCount = resourceUsage.getMemberCount();

        if (currentMemberCount >= maxAllowedMemberCount) {
            if (isExpired()) {
                throw new MryException(MEMBER_COUNT_LIMIT_REACHED,
                        "上传失败，当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，且当前成员总数已达免费版上限(" + maxAllowedMemberCount + ")，无法继续上传，如需继续请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(MEMBER_COUNT_LIMIT_REACHED,
                    "上传失败，成员总数已达当前套餐(" + currentPlanName() + ")上限(" + maxAllowedMemberCount + ")，如需继续请及时升级。",
                    mapOf("tenantId", tenantId()));
        }

        return maxAllowedMemberCount - currentMemberCount;//返回可上传数量
    }

    public boolean isMaxMemberReached() {
        int maxAllowedMemberCount = packages.effectiveMaxMemberCount();
        int currentMemberCount = resourceUsage.getMemberCount();
        return currentMemberCount >= maxAllowedMemberCount;
    }

    public void validateUpdateLogo() {
        if (!packages.customLogoAllowed()) {
            if (isExpired()) {
                throw new MryException(UPDATE_LOGO_NOT_ALLOWED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，无法设置Logo，如需设置请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(UPDATE_LOGO_NOT_ALLOWED,
                    "当前套餐(" + currentPlanName() + ")无法设置Logo，如需设置请及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public void validateAssignmentAllowed() {
        if (!packages.assignmentAllowed()) {
            if (isExpired()) {
                throw new MryException(ASSIGNMENT_NOT_ALLOWED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，无法使用任务管理功能，如需使用请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(ASSIGNMENT_NOT_ALLOWED,
                    "当前套餐(" + currentPlanName() + ")无法使用任务管理功能，如需使用请及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public void validateAppNewlyAddControls(Set<ControlType> controlTypes) {
        if (isEmpty(controlTypes)) {
            return;
        }

        if (!packages.effectiveSupportedControlTypes().containsAll(controlTypes)) {
            if (isExpired()) {
                throw new MryException(CONTROL_TYPES_NOT_ALLOWED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，对于有些新添加控件类型不支持，如需添加请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(CONTROL_TYPES_NOT_ALLOWED,
                    "当前套餐(" + currentPlanName() + ")对于有些新添加控件类型不支持，如需添加请及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public void validateUpdateSubdomain() {
        if (!packages.customSubdomainAllowed()) {
            if (isExpired()) {
                throw new MryException(UPDATE_SUBDOMAIN_NOT_ALLOWED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，无法设置子域名，如需设置请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(UPDATE_SUBDOMAIN_NOT_ALLOWED,
                    "当前套餐(" + currentPlanName() + ")无法设置子域名，如需设置请及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public void validateRefreshApiSecret() {
        if (!packages.developerAllowed()) {
            if (isExpired()) {
                throw new MryException(REFRESH_API_SECRET_NOT_ALLOWED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，无法刷新API Secret，如需刷新请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(REFRESH_API_SECRET_NOT_ALLOWED,
                    "当前套餐(" + currentPlanName() + ")无法刷新API Secret，如需刷新请及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public void validateUpdateWebhookSetting() {
        if (!packages.developerAllowed()) {
            if (isExpired()) {
                throw new MryException(UPDATE_WEBHOOK_SETTING_NOT_ALLOWED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，无法集成Webhook功能，如需继续请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(UPDATE_WEBHOOK_SETTING_NOT_ALLOWED,
                    "当前套餐(" + currentPlanName() + ")无法集成Webhook功能，如需继续请及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public void validateReporting() {
        if (!packages.reportingAllowed()) {
            if (isExpired()) {
                throw new MryException(REPORTING_NOT_ALLOWED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，无法使用报表功能，如需使用请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(REPORTING_NOT_ALLOWED,
                    "当前套餐(" + currentPlanName() + ")无法使用报表功能，如需使用请及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public void validateKanban() {
        if (!packages.kanbanAllowed()) {
            if (isExpired()) {
                throw new MryException(KANBAN_NOT_ALLOWED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，无法使用状态看板功能，如需使用请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(KANBAN_NOT_ALLOWED,
                    "当前套餐(" + currentPlanName() + ")无法使用状态看板功能，如需使用请及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public void validateCopyApp(App app) {
        Set<ControlType> appControlTypes = app.allControls().stream().map(Control::getType).collect(toImmutableSet());

        if (!packages.effectiveSupportedControlTypes().containsAll(appControlTypes)) {
            if (isExpired()) {
                throw new MryException(COPY_APP_NOT_ALLOWED,
                        "复制失败，当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，而应用中包含需要更高级别套餐的控件，如需继续请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(COPY_APP_NOT_ALLOWED,
                    "复制失败，当前套餐(" + currentPlanName() + ")级别过低，而应用中包含需要更高级别套餐的控件，如需继续请及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public void validateAddApp() {
        if (isMaxAppReached()) {
            if (isExpired()) {
                throw new MryException(APP_COUNT_LIMIT_REACHED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，无法新建应用，如需继续请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(APP_COUNT_LIMIT_REACHED,
                    "新建应用失败，应用总数已经达到当前套餐(" +
                    currentPlanName() + ")的上限(" + packages.effectiveMaxAppCount() + "个)，如需继续请及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public boolean isMaxAppReached() {
        if (MRY_MANAGE_TENANT_ID.equals(id) || MRY_APP_TEMPLATE_TENANT_ID.equals(id)) {
            return false;
        }

        int maxAllowedAppCount = packages.effectiveMaxAppCount();
        int currentAppCount = resourceUsage.getAppCount();
        return currentAppCount >= maxAllowedAppCount;
    }

    public void validCreateAppFromTemplate(QR templateQr) {
        String planTypeId = ((DropdownAttributeValue) templateQr.getAttributeValues()
                .get(TEMPLATE_PLAN_TYPE_ATTRIBUTE_ID)).getOptionIds().get(0);
        PlanType templateRequiredPlanType = OPTION_TO_PLAN_MAP.get(planTypeId);

        if (!packages.effectivePlanType().covers(templateRequiredPlanType)) {
            if (isExpired()) {
                throw new MryException(LOW_PLAN_FOR_APP_TEMPLATE,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，无法使用该应用模板，如需继续请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(LOW_PLAN_FOR_APP_TEMPLATE,
                    "当前套餐版本级别过低，无法使用该应用模板，如需继续请及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public void validateAddPlate() {
        int currentPlateCount = resourceUsage.getPlateCount();

        if (currentPlateCount >= MAX_PLATE_SIZE) {//任何租户的plate总量不能超过1亿
            throw new MryException(PLATE_COUNT_LIMIT_REACHED, "码牌总量不能超过1亿个。", mapOf("tenantId", tenantId()));
        }
    }

    public void validateAddGroup(String appId) {
        int allowedGroupCountPerApp = packages.effectiveMaxGroupCountPerApp();
        int currentGroupCount = resourceUsage.getGroupCountForApp(appId);

        if (currentGroupCount >= allowedGroupCountPerApp) {
            if (isExpired()) {
                throw new MryException(GROUP_COUNT_LIMIT_REACHED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，无法继续添加，如需添加请及时续费或升级。",
                        mapOf("tenantId", tenantId(), "appId", appId));
            }
            throw new MryException(GROUP_COUNT_LIMIT_REACHED,
                    "添加失败，总数已达当前套餐(" + currentPlanName() + ")上限(" + allowedGroupCountPerApp + ")，如需添加请及时升级。",
                    mapOf("tenantId", tenantId(), "appId", appId));
        }
    }

    public void validateAddDepartment() {
        int allowedGroupCountPerApp = packages.effectiveMaxDepartmentCount();
        int currentDepartmentCount = resourceUsage.getDepartmentCount();

        if (currentDepartmentCount >= allowedGroupCountPerApp) {
            if (isExpired()) {
                throw new MryException(DEPARTMENT_COUNT_LIMIT_REACHED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，无法继续添加部门，如需添加请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(DEPARTMENT_COUNT_LIMIT_REACHED,
                    "添加失败，总数已达当前套餐(" + currentPlanName() + ")上限(" + allowedGroupCountPerApp + ")，如需添加请及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public void validateAddQr() {
        int allowedQrCount = packages.effectiveMaxQrCount();
        int currentQrCount = resourceUsage.allQrCount();

        if (currentQrCount >= allowedQrCount) {
            if (isExpired()) {
                throw new MryException(QR_COUNT_LIMIT_REACHED,
                        "添加失败，当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，且当前总数已达免费版上限(" + allowedQrCount + ")，无法继续添加，如需添加请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(QR_COUNT_LIMIT_REACHED,
                    "添加失败，总数已达当前套餐(" + currentPlanName() + ")上限(" + allowedQrCount + ")，如需添加请及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public void validateImportQr() {
        if (!this.packages.batchImportQrAllowed()) {
            if (isExpired()) {
                throw new MryException(BATCH_QR_IMPORT_NOT_ALLOWED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，无法使用批量导入功能，请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(BATCH_QR_IMPORT_NOT_ALLOWED,
                    "当前套餐(" + currentPlanName() + ")无法使用批量导入功能，请及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public int validateImportQrs() {
        int allowedQrCount = packages.effectiveMaxQrCount();
        int currentQrCount = resourceUsage.allQrCount();

        if (currentQrCount >= allowedQrCount) {
            if (isExpired()) {
                throw new MryException(QR_COUNT_LIMIT_REACHED,
                        "上传失败，当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，且当前总数已达免费版上限(" + allowedQrCount + ")，无法继续上传，如需继续请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(QR_COUNT_LIMIT_REACHED,
                    "上传失败，总数已达当前套餐(" + currentPlanName() + ")上限(" + allowedQrCount + ")，如需继续请及时升级。",
                    mapOf("tenantId", tenantId()));
        }

        return allowedQrCount - currentQrCount;//返回可上传数量
    }

    public void validateAddSubmission() {
        int maxAllowedSubmissionCount = packages.effectiveMaxSubmissionCount();
        int currentSubmissionCount = resourceUsage.allSubmissionCount();
        if (currentSubmissionCount >= maxAllowedSubmissionCount) {
            if (isExpired()) {
                throw new MryException(SUBMISSION_COUNT_LIMIT_REACHED,
                        "提交失败，当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，且提交量已达本月上限，请联系系统管理员及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(SUBMISSION_COUNT_LIMIT_REACHED,
                    "提交失败，本月提交已达当前套餐(" + currentPlanName() + ")上限(" + maxAllowedSubmissionCount + ")，请联系系统管理员及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public void validateRequestOssToken() {
        float maxAllowedStorage = packages.effectiveMaxStorage();
        float usedStorage = resourceUsage.getStorage();

        if (usedStorage >= maxAllowedStorage) {
            if (isExpired()) {
                throw new MryException(MAX_STORAGE_REACHED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，且存储空间已用尽，请及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }
            throw new MryException(MAX_STORAGE_REACHED,
                    "当前套餐(" + currentPlanName() + ")的存储空间已用尽，请及时升级或增购存储空间。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public void validateApproveSubmission() {
        if (!this.packages.submissionApprovalAllowed()) {
            if (isExpired()) {
                throw new MryException(SUBMISSION_APPROVE_NOT_ALLOWED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，无法审批表单，请联系系统管理员及时续费或升级。",
                        mapOf("tenantId", tenantId()));
            }

            throw new MryException(SUBMISSION_APPROVE_NOT_ALLOWED,
                    "当前套餐(" + currentPlanName() + ")无法审批表单，请联系系统管理员及时升级。",
                    mapOf("tenantId", tenantId()));
        }
    }

    public boolean isMaxSmsCountReached() {
        if (resourceUsage.getSmsSentCountForCurrentMonth() < packages.effectiveMaxSmsCountPerMonth()) {
            return false;
        }

        return packages.getExtraRemainSmsCount() <= 0;
    }

    public void validateControlType(ControlType type) {
        if (!this.packages.effectiveSupportedControlTypes().contains(type)) {
            if (isExpired()) {
                throw new MryException(CONTROL_TYPE_NOT_ALLOWED,
                        "当前套餐(" + currentPlanName() + ")已过期，有效套餐已降为免费版，不再支持该控件类型，请联系系统管理员及时续费或升级。",
                        mapOf("tenantId", tenantId(), "controlType", type.name()));
            }
            throw new MryException(CONTROL_TYPE_NOT_ALLOWED,
                    "当前套餐(" + currentPlanName() + ")不支持该控件类型，请联系系统管理员及时升级。",
                    mapOf("tenantId", tenantId(), "controlType", type.name()));
        }
    }

    private String tenantId() {
        return id;
    }

    private String currentPlanName() {
        return packages.currentPlanName();
    }

    private boolean isExpired() {
        return packages.isExpired();
    }
}
