package com.mryqr.core.plan.query;

import com.mryqr.core.plan.domain.Plan;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.core.plan.domain.Plan.*;
import static com.mryqr.core.plan.query.QEnabledFeature.*;

public class PlanQueryService {
    private static final List<QListPlan> allPlansInfo = buildAllPlans();

    public static List<QListPlan> listPlans() {
        return allPlansInfo;
    }

    private static List<QListPlan> buildAllPlans() {
        List<QEnabledFeature> freePlanFeatures = enabledFeaturesFor(FREE_PLAN);
        QListPlan freePlan = QListPlan.builder()
                .type(FREE_PLAN.getType())
                .name(FREE_PLAN.name())
                .level(FREE_PLAN.getType().getLevel())
                .shortIntro("满足个人，小微企业基本需求")
                .price(FREE_PLAN.price())
                .maxAppCount(FREE_PLAN.getMaxAppCount())
                .maxMemberCount(FREE_PLAN.getMaxMemberCount())
                .maxStorage(FREE_PLAN.getMaxStorage())
                .maxSmsCountPerMonth(FREE_PLAN.getMaxSmsCountPerMonth())
                .maxQrCount(FREE_PLAN.getMaxQrCount())
                .maxDepartmentCount(FREE_PLAN.getMaxDepartmentCount())
                .maxGroupCountPerApp(FREE_PLAN.getMaxGroupCountPerApp())
                .maxSubmissionCount(FREE_PLAN.getMaxSubmissionCount())
                .maxVideoTrafficPerMonth(FREE_PLAN.getMaxVideoTrafficPerMonth())
                .controlTypes(FREE_PLAN.getSupportedControlTypes())
                .addedKeyFeatures(freePlanFeatures.stream().limit(5).collect(toImmutableList()))
                .allFeatures(freePlanFeatures)
                .build();

        List<QEnabledFeature> basicPlanFeatures = enabledFeaturesFor(BASIC_PLAN);

        QListPlan basicPlan = QListPlan.builder()
                .type(BASIC_PLAN.getType())
                .name(BASIC_PLAN.name())
                .level(BASIC_PLAN.getType().getLevel())
                .shortIntro("满足中小企业小规模使用")
                .price(BASIC_PLAN.price())
                .maxAppCount(BASIC_PLAN.getMaxAppCount())
                .maxMemberCount(BASIC_PLAN.getMaxMemberCount())
                .maxStorage(BASIC_PLAN.getMaxStorage())
                .maxSmsCountPerMonth(BASIC_PLAN.getMaxSmsCountPerMonth())
                .maxQrCount(BASIC_PLAN.getMaxQrCount())
                .maxDepartmentCount(BASIC_PLAN.getMaxDepartmentCount())
                .maxGroupCountPerApp(BASIC_PLAN.getMaxGroupCountPerApp())
                .maxSubmissionCount(BASIC_PLAN.getMaxSubmissionCount())
                .maxVideoTrafficPerMonth(BASIC_PLAN.getMaxVideoTrafficPerMonth())
                .controlTypes(BASIC_PLAN.getSupportedControlTypes())
                .addedKeyFeatures(basicPlanFeatures.stream()
                        .filter(feature -> !freePlanFeatures.contains(feature))
                        .limit(5).collect(toImmutableList()))
                .allFeatures(basicPlanFeatures)
                .build();

        List<QEnabledFeature> advancedPlanFeatures = enabledFeaturesFor(ADVANCED_PLAN);
        QListPlan advancedPlan = QListPlan.builder()
                .type(ADVANCED_PLAN.getType())
                .name(ADVANCED_PLAN.name())
                .level(ADVANCED_PLAN.getType().getLevel())
                .shortIntro("满足多数企业需求，超值推荐！")
                .price(ADVANCED_PLAN.price())
                .maxAppCount(ADVANCED_PLAN.getMaxAppCount())
                .maxMemberCount(ADVANCED_PLAN.getMaxMemberCount())
                .maxStorage(ADVANCED_PLAN.getMaxStorage())
                .maxSmsCountPerMonth(ADVANCED_PLAN.getMaxSmsCountPerMonth())
                .maxQrCount(ADVANCED_PLAN.getMaxQrCount())
                .maxDepartmentCount(ADVANCED_PLAN.getMaxDepartmentCount())
                .maxGroupCountPerApp(ADVANCED_PLAN.getMaxGroupCountPerApp())
                .maxSubmissionCount(ADVANCED_PLAN.getMaxSubmissionCount())
                .maxVideoTrafficPerMonth(ADVANCED_PLAN.getMaxVideoTrafficPerMonth())
                .controlTypes(ADVANCED_PLAN.getSupportedControlTypes())
                .addedKeyFeatures(advancedPlanFeatures.stream()
                        .filter(feature -> !basicPlanFeatures.contains(feature))
                        .limit(5).collect(toImmutableList()))
                .allFeatures(advancedPlanFeatures)
                .build();

        List<QEnabledFeature> professionalPlanFeatures = enabledFeaturesFor(PROFESSIONAL_PLAN);
        QListPlan professionalPlan = QListPlan.builder()
                .type(PROFESSIONAL_PLAN.getType())
                .name(PROFESSIONAL_PLAN.name())
                .level(PROFESSIONAL_PLAN.getType().getLevel())
                .shortIntro("满足各型企业使用，可技术对接")
                .price(PROFESSIONAL_PLAN.price())
                .maxAppCount(PROFESSIONAL_PLAN.getMaxAppCount())
                .maxMemberCount(PROFESSIONAL_PLAN.getMaxMemberCount())
                .maxStorage(PROFESSIONAL_PLAN.getMaxStorage())
                .maxSmsCountPerMonth(PROFESSIONAL_PLAN.getMaxSmsCountPerMonth())
                .maxQrCount(PROFESSIONAL_PLAN.getMaxQrCount())
                .maxDepartmentCount(PROFESSIONAL_PLAN.getMaxDepartmentCount())
                .maxGroupCountPerApp(PROFESSIONAL_PLAN.getMaxGroupCountPerApp())
                .maxSubmissionCount(PROFESSIONAL_PLAN.getMaxSubmissionCount())
                .maxVideoTrafficPerMonth(PROFESSIONAL_PLAN.getMaxVideoTrafficPerMonth())
                .controlTypes(PROFESSIONAL_PLAN.getSupportedControlTypes())
                .addedKeyFeatures(professionalPlanFeatures.stream()
                        .filter(feature -> !advancedPlanFeatures.contains(feature))
                        .limit(5).collect(toImmutableList()))
                .allFeatures(professionalPlanFeatures)
                .build();

        List<QEnabledFeature> flagshipPlanFeatures = enabledFeaturesFor(FLAGSHIP_PLAN);
        QListPlan flagshipPlan = QListPlan.builder()
                .type(FLAGSHIP_PLAN.getType())
                .name(FLAGSHIP_PLAN.name())
                .level(FLAGSHIP_PLAN.getType().getLevel())
                .shortIntro("满足企业大规模使用")
                .price(FLAGSHIP_PLAN.price())
                .maxAppCount(FLAGSHIP_PLAN.getMaxAppCount())
                .maxMemberCount(FLAGSHIP_PLAN.getMaxMemberCount())
                .maxStorage(FLAGSHIP_PLAN.getMaxStorage())
                .maxSmsCountPerMonth(FLAGSHIP_PLAN.getMaxSmsCountPerMonth())
                .maxQrCount(FLAGSHIP_PLAN.getMaxQrCount())
                .maxDepartmentCount(FLAGSHIP_PLAN.getMaxDepartmentCount())
                .maxGroupCountPerApp(FLAGSHIP_PLAN.getMaxGroupCountPerApp())
                .maxSubmissionCount(FLAGSHIP_PLAN.getMaxSubmissionCount())
                .maxVideoTrafficPerMonth(FLAGSHIP_PLAN.getMaxVideoTrafficPerMonth())
                .controlTypes(FLAGSHIP_PLAN.getSupportedControlTypes())
                .addedKeyFeatures(flagshipPlanFeatures.stream()
                        .filter(feature -> !professionalPlanFeatures.contains(feature))
                        .limit(5).collect(toImmutableList()))
                .allFeatures(flagshipPlanFeatures)
                .build();

        return List.of(freePlan, basicPlan, advancedPlan, professionalPlan, flagshipPlan);
    }

    private static List<QEnabledFeature> enabledFeaturesFor(Plan plan) {
        List<QEnabledFeature> features = new ArrayList<>(List.of(
                QR_NO_EXPIRE,
                FORM_CUSTOMIZABLE,
                PLATE_CUSTOMIZABLE,
                PC_OPERATIONS,
                MOBILE_OPERATIONS,
                CUSTOM_ATTRIBUTE,
                CUSTOM_OPS_MENU,
                GEO_PREVENT_FRAUD,
                PHOTO_PREVENT_FRAUD,
                PLATE_IMAGE,
                QR_EXCEL_EXPORT,
                SUBMISSION_EXCEL_EXPORT,
                FORM_PERMISSION,
                CONTROL_PERMISSION,
                OPS_PERMISSION
        ));

        if (plan.isReportingAllowed()) {
            features.add(APP_REPORTING);
        }

        if (plan.isKanbanAllowed()) {
            features.add(KANBAN);
        }

        if (plan.isCustomSubdomainAllowed()) {
            features.add(CUSTOM_SUBDOMAIN);
            features.add(CUSTOM_LOGIN_BACKGROUND);
        }

        if (plan.isCustomLogoAllowed()) {
            features.add(CUSTOM_LOGO);
        }

        if (plan.isSubmissionApprovalAllowed()) {
            features.add(APPROVAL_ENABLED);
        }

        if (plan.isAssignmentAllowed()) {
            features.add(ASSIGNMENT_ENABLED);
        }

        if (plan.isHideBottomMryLogo()) {
            features.add(HIDE_BOTTOM_MRY_LOGO);
        }

        if (plan.isVideoAudioAllowed()) {
            features.add(VIDEO_AUDIO_ALLOWED);
        }

        if (plan.isHideAds()) {
            features.add(HIDE_ADS);
        }

        if (plan.isDeveloperAllowed()) {
            features.add(API_ENABLED);
            features.add(WEBHOOK_ENABLED);
        }

        if (plan.isSubmissionNotifyAllowed()) {
            features.add(SUBMISSION_NOTIFY);
        }

        if (plan.isBatchImportMemberAllowed()) {
            features.add(MEMBER_BATCH_UPLOAD);
        }

        if (plan.isBatchImportQrAllowed()) {
            features.add(QR_BATCH_UPLOAD);
        }

        return List.copyOf(features);
    }

}
