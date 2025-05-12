package com.mryqr.core.qr.query.submission;

import com.mryqr.core.common.domain.permission.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QSubmissionQr {
    private final QSubmissionQrDetail qr;
    private final QSubmissionAppDetail app;
    private final QSubmissionQrMemberProfile submissionQrMemberProfile;
    private final Set<Permission> permissions;
    private final Set<String> canViewFillablePageIds;
    private final Set<String> canManageFillablePageIds;
    private final Set<String> canApproveFillablePageIds;
    private final boolean canOperateApp;
}
