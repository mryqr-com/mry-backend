package com.mryqr.core.plan.query;

import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.plan.domain.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QListPlan {
    private final PlanType type;
    private final String name;
    private final int level;
    private final String shortIntro;
    private final int price;
    private final int maxAppCount;
    private final int maxMemberCount;
    private final float maxStorage;
    private final int maxSmsCountPerMonth;
    private final int maxSubmissionCount;
    private final int maxQrCount;
    private final int maxDepartmentCount;
    private final int maxGroupCountPerApp;
    private final int maxVideoTrafficPerMonth;
    private final Set<ControlType> controlTypes;
    private final List<QEnabledFeature> addedKeyFeatures;
    private final List<QEnabledFeature> allFeatures;
}
