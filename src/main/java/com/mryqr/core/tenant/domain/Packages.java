package com.mryqr.core.tenant.domain;

import com.mryqr.common.exception.MryException;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.plan.domain.Plan;
import com.mryqr.core.plan.domain.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Set;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.plan.domain.Plan.FREE_PLAN;
import static com.mryqr.core.plan.domain.Plan.planFor;
import static com.mryqr.core.plan.domain.PlanType.FREE;
import static java.time.Instant.now;
import static java.time.LocalDate.of;
import static java.time.ZoneId.systemDefault;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public final class Packages {
    private static final int MAX_PLAN_DURATION_YEARS = 2;
    private static final int MAX_EXTRA_TENANT_MEMBERS = 10000;
    private static final int MAX_EXTRA_STORAGES = 10000;
    private static final int MAX_EXTRA_VIDEO_TRAFFIC = 10000;
    private static final Instant FREE_PLAN_EXPIRE_AT = of(2099, 12, 31).atStartOfDay(systemDefault()).toInstant();

    private Plan plan;//当前的套餐方案
    private Instant expireAt;//过期时间
    private String planVersion;//当前套餐版本

    private int extraMemberCount;//增购成员数量，永久生效
    private int extraStorage;//增购存储容量，永久生效
    private int extraRemainSmsCount;//增购短信量，永久生效
    private float extraRemainVideoTraffic;//剩余增购容量(单位GB)，永久生效

    public static Packages init() {
        return Packages.builder()
                .plan(planFor(FREE))
                .expireAt(FREE_PLAN_EXPIRE_AT)
                .planVersion(newShortUuid())
                .build();
    }

    public void updatePlanType(PlanType planType, Instant expireAt) {
        requireNonNull(planType, "Plan type cannot be null.");
        requireNonNull(expireAt, "Expire at cannot be null.");

        this.plan = planFor(planType);
        this.expireAt = expireAt;
        this.planVersion = newShortUuid();
    }

    public void updatePlan(Plan plan) {
        requireNonNull(plan, "Plan cannot be null.");

        this.plan = plan;
        this.planVersion = newShortUuid();
    }


    public void increaseExtraMemberCount(int amount) {
        this.extraMemberCount = this.extraMemberCount + amount;
    }

    public void increaseExtraStorage(int amount) {
        this.extraStorage = this.extraStorage + amount;
    }

    public void increaseExtraVideoTraffic(int amount) {
        this.extraRemainVideoTraffic = this.extraRemainVideoTraffic + amount;
    }

    public void increaseExtraRemainSmsCount(int amount) {
        this.extraRemainSmsCount = this.extraRemainSmsCount + amount;
    }

    public void tryUseExtraRemainSms() {
        if (this.extraRemainSmsCount > 0) {
            this.extraRemainSmsCount--;
        }
    }

    public Instant expireAt() {
        return expireAt;
    }

    public boolean isExpired() {
        return this.currentPlanType() != FREE && now().isAfter(expireAt);
    }

    public String currentPlanName() {
        return this.plan.name();
    }

    public Plan currentPlan() {
        return this.plan;
    }

    public PlanType currentPlanType() {
        return plan.getType();
    }

    //当前有效套餐，要么为真实套餐，要么为免费套餐（真实套餐过期后即自动降级为免费套餐即通过这里完成）
    public Plan effectivePlan() {
        return isExpired() ? FREE_PLAN : plan;
    }

    public PlanType effectivePlanType() {
        return effectivePlan().getType();
    }

    public boolean isEffectiveFreePlan() {
        return effectivePlanType() == FREE;
    }

    public boolean submissionNotifyAllowed() {
        return effectivePlan().isSubmissionNotifyAllowed();
    }

    public boolean batchImportQrAllowed() {
        return effectivePlan().isBatchImportQrAllowed();
    }

    public boolean batchImportMemberAllowed() {
        return effectivePlan().isBatchImportMemberAllowed();
    }

    public boolean submissionApprovalAllowed() {
        return effectivePlan().isSubmissionApprovalAllowed();
    }

    public boolean reportingAllowed() {
        return effectivePlan().isReportingAllowed();
    }

    public boolean kanbanAllowed() {
        return effectivePlan().isKanbanAllowed();
    }

    public boolean developerAllowed() {
        return effectivePlan().isDeveloperAllowed();
    }

    public boolean customLogoAllowed() {
        return effectivePlan().isCustomLogoAllowed();
    }

    public boolean videoAudioAllowed() {
        return effectivePlan().isVideoAudioAllowed();
    }

    public boolean assignmentAllowed() {
        return effectivePlan().isAssignmentAllowed();
    }

    public boolean customSubdomainAllowed() {
        return effectivePlan().isCustomSubdomainAllowed();
    }

    public boolean hideBottomMryLogo() {
        return effectivePlan().isHideBottomMryLogo();
    }

    public int effectiveMaxMemberCount() {
        return effectivePlan().getMaxMemberCount() + extraMemberCount;
    }

    public float effectiveMaxStorage() {
        return effectivePlan().getMaxStorage() + extraStorage;
    }

    public int effectiveMaxSmsCountPerMonth() {
        return effectivePlan().getMaxSmsCountPerMonth();
    }

    public int effectiveMaxAppCount() {
        return effectivePlan().getMaxAppCount();
    }

    public int effectiveMaxDepartmentCount() {
        return effectivePlan().getMaxDepartmentCount();
    }

    public int effectiveMaxGroupCountPerApp() {
        return effectivePlan().getMaxGroupCountPerApp();
    }

    public int effectiveMaxQrCount() {
        return effectivePlan().getMaxQrCount();
    }

    public int effectiveMaxSubmissionCount() {
        return effectivePlan().getMaxSubmissionCount();
    }

    public String planVersion() {
        return this.planVersion;
    }

    public Instant calculateExpirationFor(int yearDuration) {
        if (effectivePlanType() == FREE) {
            return ZonedDateTime.now().plusYears(yearDuration).toInstant();
        }

        return ZonedDateTime.ofInstant(expireAt, systemDefault()).plusYears(yearDuration).toInstant();
    }

    public void validateAddPlanDuration(int yearDuration) {
        Instant expiredAt = calculateExpirationFor(yearDuration);
        Instant fiveYearsLater = ZonedDateTime.now().plusYears(MAX_PLAN_DURATION_YEARS).toInstant();

        if (expiredAt.isAfter(fiveYearsLater)) {
            throw new MryException(PACKAGE_DURATION_TOO_LONG, "套餐总时长不能超过" + MAX_PLAN_DURATION_YEARS + "年。");
        }
    }

    public void validateAddExtraMembers(int amount) {
        if (this.extraMemberCount + amount > MAX_EXTRA_TENANT_MEMBERS) {
            throw new MryException(MAX_TENANT_MEMBER_SIZE_REACHED, "增购成员总数最多不超过" + MAX_EXTRA_TENANT_MEMBERS + "名。");
        }
    }

    public void validateAddExtraStorage(int amount) {
        if (this.extraStorage + amount > MAX_EXTRA_STORAGES) {
            throw new MryException(MAX_EXTRA_STORAGE_REACHED, "增购存储空间总量不超过" + MAX_EXTRA_STORAGES + "G。");
        }
    }

    public void validateAddExtraVideoTraffic(int amount) {
        if (this.extraRemainVideoTraffic + amount > MAX_EXTRA_VIDEO_TRAFFIC) {
            throw new MryException(MAX_VIDEO_TRAFFIC_REACHED, "增购视频总流量不超过" + MAX_EXTRA_VIDEO_TRAFFIC + "G。");
        }
    }

    public int getExtraMemberCount() {
        return extraMemberCount;
    }

    public int getExtraStorage() {
        return extraStorage;
    }

    public int getExtraRemainSmsCount() {
        return extraRemainSmsCount;
    }

    public float getExtraRemainVideoTraffic() {
        return extraRemainVideoTraffic;
    }

    public Set<ControlType> effectiveSupportedControlTypes() {
        return this.effectivePlan().getSupportedControlTypes();
    }
}
