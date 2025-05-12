package com.mryqr.core.tenant.domain;

import com.mryqr.common.domain.AggregateRoot;
import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.domain.invoice.InvoiceTitle;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.core.order.domain.Order;
import com.mryqr.core.order.domain.delivery.Consignee;
import com.mryqr.core.order.domain.detail.*;
import com.mryqr.core.plan.domain.Plan;
import com.mryqr.core.plan.domain.PlanType;
import com.mryqr.core.tenant.domain.event.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.Identified.isDuplicated;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.TENANT_COLLECTION;
import static com.mryqr.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_TENANT_ID;
import static java.time.LocalDate.ofInstant;
import static java.time.ZoneId.systemDefault;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Getter
@FieldNameConstants
@Document(TENANT_COLLECTION)
@TypeAlias(TENANT_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class Tenant extends AggregateRoot {
    private String name;//租户名称
    private Packages packages;//当前套餐
    private ResourceUsage resourceUsage;//当前资源使用量统计
    private UploadedFile logo;//Logo
    private UploadedFile loginBackground;//登录背景图片
    private ApiSetting apiSetting;//API集成设置
    private boolean active;//用于后台管理端设置，非active时所有成员无法登录，无法访问API
    private InvoiceTitle invoiceTitle;//发票抬头
    private List<Consignee> consignees;//收货人
    private Instant recentAccessedAt;//最近活跃时间

    public Tenant(String name, User user) {
        super(user.getTenantId(), user);
        this.name = name;
        this.packages = Packages.init();
        this.resourceUsage = ResourceUsage.init();
        this.apiSetting = ApiSetting.init();
        this.active = true;
        this.consignees = new ArrayList<>(3);
        this.raiseEvent(new TenantCreatedEvent(this.getId(), user));
        addOpsLog("注册", user);
    }

    public static String newTenantId() {
        return "TNT" + newSnowflakeId();
    }

    public void updateBaseSetting(String name, UploadedFile loginBackground, User user) {
        this.name = name;
        this.loginBackground = loginBackground;
        raiseEvent(new TenantBaseSettingUpdatedEvent(this.getId(), user));
        addOpsLog("更新基本设置", user);
    }

    public void updateLogo(UploadedFile logo, User us
    ) {
        if (Objects.equals(this.logo, logo)) {
            return;
        }

        this.logo = logo;
        addOpsLog("更新Logo", us);
    }

    public void activate(User user) {
        if (this.active) {
            return;
        }

        this.active = true;
        raiseEvent(new TenantActivatedEvent(this.getId(), user));
        addOpsLog("启用", user);
    }

    public void deactivate(User user) {
        if (!this.active) {
            return;
        }

        this.active = false;
        raiseEvent(new TenantDeactivatedEvent(this.getId(), user));
        addOpsLog("禁用", user);
    }

    public void refreshApiSecret(User user) {
        this.apiSetting.refreshApiSecret();
        addOpsLog("刷新API Secret", user);
    }

    public void updateInvoiceTitle(InvoiceTitle title, User user) {
        if (Objects.equals(title, this.invoiceTitle)) {
            return;
        }
        this.invoiceTitle = title;
        raiseEvent(new TenantInvoiceTitleUpdatedEvent(this.getId(), user));
        addOpsLog("更新发票抬头", user);
    }

    public void updatePlanType(PlanType planType, Instant expireAt, User user) {
        this.packages.updatePlanType(planType, expireAt);
        raiseEvent(new TenantPlanUpdatedEvent(this.getId(), planType, user));
        addOpsLog("设置套餐为" + planType.getName() + "(" + ofInstant(expireAt, systemDefault()) + "过期)", user);
    }

    public void updatePlan(Plan plan, User user) {
        this.packages.updatePlan(plan);
        raiseEvent(new TenantPlanUpdatedEvent(this.getId(), plan.getType(), user));
        addOpsLog("更新套餐", user);
    }

    public Plan effectivePlan() {
        return this.packages.effectivePlan();
    }

    public PlanType effectivePlanType() {
        return this.packages.effectivePlanType();
    }

    public Plan currentPlan() {
        return this.packages.currentPlan();
    }

    public PlanType currentPlanType() {
        return this.packages.currentPlanType();
    }

    public boolean isPackagesExpired() {
        return this.packages.isExpired();
    }

    public Instant packagesExpiredAt() {
        return this.packages.expireAt();
    }

    public String planVersion() {
        return this.packages.planVersion();
    }

    public boolean isEffectiveFreePlan() {
        return this.packages.isEffectiveFreePlan();
    }

    public void validateAddExtraMembers(int amount) {
        this.packages.validateAddExtraMembers(amount);
    }

    public void validateAddPlanDuration(int yearDuration) {
        this.packages.validateAddPlanDuration(yearDuration);
    }

    public void validateAddExtraVideoTraffic(int amount) {
        this.packages.validateAddExtraVideoTraffic(amount);
    }

    public void validateAddExtraStorage(int amount) {
        this.packages.validateAddExtraStorage(amount);
    }

    public void applyOrder(Order order, User user) {
        OrderDetail orderDetail = order.getDetail();
        switch (orderDetail.getType()) {
            case PLAN -> {
                if (Objects.equals(order.getPlanVersion(), packages.planVersion())) {
                    PlanOrderDetail detail = (PlanOrderDetail) orderDetail;
                    this.packages.updatePlanType(detail.getPlanType(), packages.calculateExpirationFor(detail.getYearDuration()));
                } else {
                    log.warn("Order[{}] plan version not match with tenant[{}] current plan version, skip applying order.",
                            order.getId(), this.getId());
                }
            }

            case EXTRA_MEMBER -> {
                ExtraMemberOrderDetail detail = (ExtraMemberOrderDetail) orderDetail;
                this.packages.increaseExtraMemberCount(detail.getAmount());
            }

            case EXTRA_SMS -> {
                ExtraSmsOrderDetail detail = (ExtraSmsOrderDetail) orderDetail;
                this.packages.increaseExtraRemainSmsCount(detail.getSmsAmount());
            }

            case EXTRA_STORAGE -> {
                ExtraStorageOrderDetail detail = (ExtraStorageOrderDetail) orderDetail;
                this.packages.increaseExtraStorage(detail.getAmount());
            }

            case EXTRA_VIDEO_TRAFFIC -> {
                ExtraVideoTrafficOrderDetail detail = (ExtraVideoTrafficOrderDetail) orderDetail;
                this.packages.increaseExtraVideoTraffic(detail.getAmount());
            }
        }

        raiseEvent(new TenantOrderAppliedEvent(this.getId(), order.getId(), user));
        this.addOpsLog("成交订单(" + orderDetail.description() + ")", user);
    }

    public void setMemberCount(int memberCount, User user) {
        this.resourceUsage.updateMemberCount(memberCount);
        raiseEvent(new TenantResourceUsageUpdatedEvent(this.getId(), user));
    }

    public void setGroupCountForApp(String appId, int groupCount) {
        this.resourceUsage.updateAppGroupCount(appId, groupCount);
    }

    public void setAppCount(int appCount, User user) {
        this.resourceUsage.updateAppCount(appCount);
        raiseEvent(new TenantResourceUsageUpdatedEvent(this.getId(), user));
    }

    public void setDepartmentCount(int appCount) {
        this.resourceUsage.updateDepartmentCount(appCount);
    }

    public void setPlateCount(int plateCount) {
        this.resourceUsage.updatePlateCount(plateCount);
    }

    public void setQrCountForApp(String appId, int qrCount) {
        this.resourceUsage.updateAppQrCount(appId, qrCount);
    }

    public void setSubmissionCountForApp(String appId, int submissionCount) {
        this.resourceUsage.updateAppSubmissionCount(appId, submissionCount);
    }

    public void setStorage(float amount, User user) {
        this.resourceUsage.setStorage(amount);
        raiseEvent(new TenantResourceUsageUpdatedEvent(this.getId(), user));
    }

    public void removeAppUsage(String appId) {
        this.resourceUsage.removeApp(appId);
    }

    public PackagesStatus packagesStatus() {
        return PackagesStatus.builder().id(this.getId()).packages(this.packages).resourceUsage(this.resourceUsage).build();
    }

    public boolean isDeveloperAllowed() {
        return this.effectivePlan().isDeveloperAllowed();
    }

    public boolean isAssignmentAllowed() {
        return this.effectivePlan().isAssignmentAllowed();
    }

    public boolean isSubmissionNotifyAllowed() {
        return this.effectivePlan().isSubmissionNotifyAllowed();
    }

    public boolean isMryManageTenant() {
        return Objects.equals(this.getId(), MRY_MANAGE_TENANT_ID);
    }

    public boolean isMryTestingTenant() {
        return Objects.equals(this.getId(), "TNT393272027126566913");
    }

    public void addConsignee(Consignee consignee, User user) {
        if (this.consignees.size() >= 5) {
            throw new MryException(MAX_CONSIGNEE_REACHED, "最多只能添加5个收货人信息。", mapOf("tenantId", this.getId()));
        }

        this.consignees.add(0, consignee);

        if (isDuplicated(this.consignees)) {
            throw new MryException(CONSIGNEE_ID_DUPLICATED, "收货人ID重复。", mapOf("tenantId", this.getId()));
        }

        addOpsLog("添加收货人(" + consignee.getName() + ")", user);
    }

    public void updateConsignee(Consignee newConsignee, User user) {
        this.consignees = this.consignees.stream()
                .map(consignee -> Objects.equals(consignee.getId(), newConsignee.getId()) ? newConsignee : consignee)
                .collect(toList());
        addOpsLog("更新收货人(" + newConsignee.getName() + ")", user);
    }

    public void deleteConsignee(String consigneeId, User user) {
        Optional<Consignee> consigneeOptional = this.consignees.stream()
                .filter(consignee -> Objects.equals(consignee.getId(), consigneeId)).findFirst();

        if (consigneeOptional.isEmpty()) {
            log.warn("No consignee[{}] found to delete, skip.", consigneeId);
            return;
        }

        this.consignees.removeIf(consignee -> Objects.equals(consignee.getId(), consigneeId));
        addOpsLog("删除收货人(" + consigneeOptional.get().getName() + ")", user);
    }

    public void checkActive() {
        if (!this.active) {
            throw new MryException(TENANT_ALREADY_DEACTIVATED, "当前账户已经被禁用。", "tenantId", this.getId());
        }
    }

    public void useSms() {
        this.resourceUsage.increaseSmsSentCountForCurrentMonth();

        if (this.resourceUsage.getSmsSentCountForCurrentMonth() > this.packages.effectiveMaxSmsCountPerMonth()) {
            this.packages.tryUseExtraRemainSms();
        }
    }
}
