package com.mryqr.core.member.query.profile;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.domain.user.Role;
import com.mryqr.core.tenant.query.QConsoleTenantProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QConsoleMemberProfile {
    private final String memberId;
    private final String tenantId;
    private final String name;
    private final Role role;
    private final UploadedFile avatar;
    private final boolean hasManagedApps;
    private final QConsoleTenantProfile tenantProfile;
    private final List<String> topAppIds;
    private final boolean mobileIdentified;
}
