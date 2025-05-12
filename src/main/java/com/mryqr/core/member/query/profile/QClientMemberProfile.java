package com.mryqr.core.member.query.profile;

import com.mryqr.core.common.domain.UploadedFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QClientMemberProfile {
    private final String memberId;
    private final String memberName;
    private final UploadedFile avatar;
    private final String tenantId;
    private final String tenantName;
    private final UploadedFile tenantLogo;
    private final String subdomainPrefix;
    private final boolean subdomainReady;
    private final List<String> topAppIds;
    private final boolean hideBottomMryLogo;
    private final boolean reportingAllowed;
    private final boolean kanbanAllowed;
    private final boolean assignmentAllowed;
}
