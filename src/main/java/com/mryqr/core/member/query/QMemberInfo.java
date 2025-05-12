package com.mryqr.core.member.query;

import com.mryqr.core.common.domain.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QMemberInfo {
    private String memberId;
    private String tenantId;
    private String name;
    private String email;
    private String mobile;
    private Role role;
    private String wxNickName;
    private boolean wxBound;
    private List<String> departments;
}
