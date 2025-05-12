package com.mryqr.core.plan.domain;

import com.mryqr.core.app.domain.page.control.ControlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.app.domain.page.control.ControlType.ATTACHMENT_VIEW;
import static com.mryqr.core.app.domain.page.control.ControlType.ATTRIBUTE_DASHBOARD;
import static com.mryqr.core.app.domain.page.control.ControlType.BAR;
import static com.mryqr.core.app.domain.page.control.ControlType.DOUGHNUT;
import static com.mryqr.core.app.domain.page.control.ControlType.ITEM_COUNT;
import static com.mryqr.core.app.domain.page.control.ControlType.MEMBER_SELECT;
import static com.mryqr.core.app.domain.page.control.ControlType.NUMBER_RANGE_SEGMENT;
import static com.mryqr.core.app.domain.page.control.ControlType.PIE;
import static com.mryqr.core.app.domain.page.control.ControlType.SIGNATURE;
import static com.mryqr.core.app.domain.page.control.ControlType.TIME_SEGMENT;
import static com.mryqr.core.app.domain.page.control.ControlType.TREND;
import static com.mryqr.core.app.domain.page.control.ControlType.VIDEO_VIEW;
import static com.mryqr.core.plan.domain.PlanType.ADVANCED;
import static com.mryqr.core.plan.domain.PlanType.BASIC;
import static com.mryqr.core.plan.domain.PlanType.FLAGSHIP;
import static com.mryqr.core.plan.domain.PlanType.FREE;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class Plan {
    private static final Set<ControlType> FREE_PLAN_EXCLUDES = Set.of(
            VIDEO_VIEW,
            ATTACHMENT_VIEW,
            ATTRIBUTE_DASHBOARD,
            BAR,
            PIE,
            TREND,
            DOUGHNUT,
            TIME_SEGMENT,
            NUMBER_RANGE_SEGMENT,

            MEMBER_SELECT,
            ITEM_COUNT,
            SIGNATURE
    );

    private static final Set<ControlType> BASIC_PLAN_EXCLUDES = Set.of(
            VIDEO_VIEW,
            TIME_SEGMENT,
            NUMBER_RANGE_SEGMENT,

            MEMBER_SELECT,
            ITEM_COUNT
    );

    public static final Plan FREE_PLAN = Plan.builder()
            .type(FREE)
            .maxAppCount(2)
            .maxQrCount(1000)
            .maxSubmissionCount(5000)
            .maxMemberCount(5)
            .maxStorage(0.1f)
            .maxSmsCountPerMonth(20)
            .maxDepartmentCount(5)
            .maxGroupCountPerApp(10)
            .maxVideoTrafficPerMonth(1)
            .supportedControlTypes(freePlanControlTypes())
            .customSubdomainAllowed(false)
            .customLogoAllowed(false)
            .hideBottomMryLogo(false)
            .hideAds(false)
            .videoAudioAllowed(false)
            .developerAllowed(false)
            .reportingAllowed(false)
            .kanbanAllowed(false)
            .submissionNotifyAllowed(false)
            .batchImportQrAllowed(false)
            .batchImportMemberAllowed(false)
            .submissionApprovalAllowed(false)
            .assignmentAllowed(false)
            .build();

    public static final Plan BASIC_PLAN = Plan.builder()
            .type(BASIC)
            .maxAppCount(5)
            .maxQrCount(10000)
            .maxSubmissionCount(50000)
            .maxMemberCount(20)
            .maxStorage(3)
            .maxSmsCountPerMonth(200)
            .maxDepartmentCount(20)
            .maxGroupCountPerApp(50)
            .maxVideoTrafficPerMonth(30)
            .supportedControlTypes(basicPlanControlTypes())
            .customSubdomainAllowed(false)
            .customLogoAllowed(true)
            .hideBottomMryLogo(false)
            .hideAds(false)
            .videoAudioAllowed(false)
            .developerAllowed(false)
            .reportingAllowed(false)
            .kanbanAllowed(true)
            .submissionNotifyAllowed(false)
            .batchImportQrAllowed(true)
            .batchImportMemberAllowed(true)
            .submissionApprovalAllowed(false)
            .assignmentAllowed(false)
            .build();

    public static final Plan ADVANCED_PLAN = Plan.builder()
            .type(ADVANCED)
            .maxAppCount(20)
            .maxQrCount(100000)
            .maxSubmissionCount(500000)
            .maxMemberCount(50)
            .maxStorage(10)
            .maxSmsCountPerMonth(500)
            .maxDepartmentCount(50)
            .maxGroupCountPerApp(100)
            .maxVideoTrafficPerMonth(60)
            .supportedControlTypes(allControlTypes())
            .customSubdomainAllowed(false)
            .customLogoAllowed(true)
            .hideBottomMryLogo(true)
            .hideAds(true)
            .videoAudioAllowed(true)
            .developerAllowed(false)
            .reportingAllowed(true)
            .kanbanAllowed(true)
            .submissionNotifyAllowed(true)
            .batchImportQrAllowed(true)
            .batchImportMemberAllowed(true)
            .submissionApprovalAllowed(true)
            .assignmentAllowed(true)
            .build();

    public static final Plan PROFESSIONAL_PLAN = Plan.builder()
            .type(PROFESSIONAL)
            .maxAppCount(50)
            .maxQrCount(300000)
            .maxSubmissionCount(1000000)
            .maxMemberCount(100)
            .maxStorage(50)
            .maxSmsCountPerMonth(1000)
            .maxDepartmentCount(100)
            .maxGroupCountPerApp(200)
            .maxVideoTrafficPerMonth(300)
            .supportedControlTypes(allControlTypes())
            .customSubdomainAllowed(true)
            .customLogoAllowed(true)
            .hideBottomMryLogo(true)
            .hideAds(true)
            .videoAudioAllowed(true)
            .developerAllowed(true)
            .reportingAllowed(true)
            .kanbanAllowed(true)
            .submissionNotifyAllowed(true)
            .batchImportQrAllowed(true)
            .batchImportMemberAllowed(true)
            .submissionApprovalAllowed(true)
            .assignmentAllowed(true)
            .build();

    public static final Plan FLAGSHIP_PLAN = Plan.builder()
            .type(FLAGSHIP)
            .maxAppCount(100)
            .maxQrCount(1000000)
            .maxSubmissionCount(3000000)
            .maxMemberCount(200)
            .maxStorage(100)
            .maxSmsCountPerMonth(3000)
            .maxDepartmentCount(200)
            .maxGroupCountPerApp(300)
            .maxVideoTrafficPerMonth(1000)
            .supportedControlTypes(allControlTypes())
            .customSubdomainAllowed(true)
            .customLogoAllowed(true)
            .hideBottomMryLogo(true)
            .hideAds(true)
            .videoAudioAllowed(true)
            .developerAllowed(true)
            .reportingAllowed(true)
            .kanbanAllowed(true)
            .submissionNotifyAllowed(true)
            .batchImportQrAllowed(true)
            .batchImportMemberAllowed(true)
            .submissionApprovalAllowed(true)
            .assignmentAllowed(true)
            .build();

    private static Map<PlanType, Plan> ALL_PLANS = Map.of(
            FREE, FREE_PLAN,
            BASIC, BASIC_PLAN,
            ADVANCED, ADVANCED_PLAN,
            PROFESSIONAL, PROFESSIONAL_PLAN,
            FLAGSHIP, FLAGSHIP_PLAN);

    private final PlanType type;
    private final int maxAppCount;//应用数量
    private final int maxQrCount;//实例总量
    private final int maxSubmissionCount;//提交总量
    private final int maxDepartmentCount;//部门总量
    private final int maxGroupCountPerApp;//单个应用下group数量
    private final int maxMemberCount;//成员数量
    private final float maxStorage;//上传文件容量，单位GB
    private final int maxSmsCountPerMonth;//每月短信用量
    private final int maxVideoTrafficPerMonth;//每月最大视频播放流量，单位GB


    private final Set<ControlType> supportedControlTypes;//可用的控件类型
    private final boolean hideBottomMryLogo;//去除页面底部码如云标识
    private final boolean hideAds;//去除广告
    private final boolean videoAudioAllowed;//是否支持上传音视频（用于文件上传控件等）
    private final boolean customSubdomainAllowed;//自定义子域名
    private final boolean customLogoAllowed;//自定义logo
    private final boolean developerAllowed;//启用API开发，包括API调用和webhook
    private final boolean reportingAllowed;//启用报表功能
    private final boolean kanbanAllowed;//状态看板
    private final boolean submissionNotifyAllowed;//提交提醒功能
    private final boolean batchImportQrAllowed;//批量导入实例数据
    private final boolean batchImportMemberAllowed;//批量导入成员数据
    private final boolean submissionApprovalAllowed;//提交审批
    private final boolean assignmentAllowed;//任务管理

    public static Plan planFor(PlanType type) {
        return ALL_PLANS.get(type);
    }

    private static Set<ControlType> allControlTypes() {
        return Set.of(ControlType.values());
    }

    private static Set<ControlType> freePlanControlTypes() {
        Set<ControlType> controlTypes = allControlTypes();
        return controlTypes.stream().filter(controlType -> !FREE_PLAN_EXCLUDES.contains(controlType)).collect(toImmutableSet());
    }

    private static Set<ControlType> basicPlanControlTypes() {
        Set<ControlType> controlTypes = allControlTypes();
        return controlTypes.stream().filter(controlType -> !BASIC_PLAN_EXCLUDES.contains(controlType)).collect(toImmutableSet());
    }

    public String name() {
        return this.type.getName();
    }

    public int price() {
        return this.type.getPrice();
    }

}
