package com.mryqr.core.tenant.query;

import com.mryqr.core.plan.domain.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QTenantInfo {
    private String tenantId;
    private String name;
    private PlanType planType;
    private Instant createdAt;
    private String createdBy;
    private String creator;
    private String packagesName;

    private int planMaxAppCount;
    private int planMaxMemberCount;
    private float planMaxStorage;

    private boolean isPackagesExpired;
    private Instant packagesExpireAt;

    private int extraMemberCount;
    private int extraStorage;
    private int extraRemainSmsCount;

    private int usedAppCount;
    private int effectiveMaxAppCount;

    private int usedMemberCount;
    private int effectiveMaxMemberCount;

    private String usedStorage;
    private String effectiveMaxStorage;

    private int usedSubmissionCount;
    private int effectiveMaxSubmissionCount;

    private int usedQrCount;
    private int effectiveMaxQrCount;

    private int usedSmsCountForCurrentMonth;
    private int effectiveMaxSmsCountPerMonth;

    private int effectiveMaxGroupCountPerApp;
    private int effectiveMaxDepartmentCount;
}
