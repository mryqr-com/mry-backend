package com.mryqr.core.member.domain;

import com.mryqr.common.domain.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static com.mryqr.common.domain.user.Role.TENANT_ADMIN;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class TenantCachedMember {
    private final String id;
    private final String name;
    private final Role role;
    private final String mobile;
    private final String email;
    private final String mobileWxOpenId;
    private final String customId;
    private final List<String> departmentIds;
    private final boolean active;

    public boolean isTenantAdmin() {
        return this.role == TENANT_ADMIN;
    }
}
