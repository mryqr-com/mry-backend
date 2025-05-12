package com.mryqr.core.tenant.query;

import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.plan.domain.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QPackagesStatus {
    private String planName;
    private PlanType planType;
    private String effectivePlanName;
    private PlanType effectivePlanType;

    private boolean maxAppReached;
    private boolean maxMemberReached;
    private boolean submissionNotifyAllowed;
    private boolean batchImportQrAllowed;
    private boolean batchImportMemberAllowed;
    private boolean submissionApprovalAllowed;
    private boolean reportingAllowed;
    private boolean customLogoAllowed;
    private boolean developerAllowed;
    private boolean videoAudioAllowed;
    private boolean assignmentAllowed;
    private Set<ControlType> supportedControlTypes;

    private boolean expired;
    private Instant expireAt;
}
