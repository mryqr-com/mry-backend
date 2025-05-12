package com.mryqr.core.qr.query.submission;

import com.mryqr.common.domain.UploadedFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QSubmissionQrMemberProfile {
    private final String memberId;
    private final String memberTenantId;
    private final String memberName;
    private final UploadedFile memberAvatar;

    //tenant表示App对应的tenant，member可能不属于tenant
    private final String tenantId;
    private final String tenantName;
    private final UploadedFile tenantLogo;
    private final String subdomainPrefix;
    private final boolean subdomainReady;
    private final boolean hideBottomMryLogo;
    private final boolean videoAudioAllowed;
}
