package com.mryqr.core.member.query;

import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.common.domain.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QListMember {
    private String id;
    private String name;
    private List<String> departmentIds;
    private UploadedFile avatar;
    private boolean active;
    private Role role;
    private String mobile;
    private String wxUnionId;
    private String wxNickName;
    private String email;
    private Instant createdAt;
}

