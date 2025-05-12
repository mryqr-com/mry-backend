package com.mryqr.integration.member.query;

import com.mryqr.core.common.domain.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QIntegrationListMember {
    private final String id;
    private final String name;
    private final Role role;
    private final String mobile;
    private final String email;
    private final boolean active;
    private final String customId;
    private final List<String> departmentIds;
}
