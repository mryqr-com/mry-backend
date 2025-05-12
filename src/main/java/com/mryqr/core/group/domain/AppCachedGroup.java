package com.mryqr.core.group.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class AppCachedGroup {
    private String id;
    private String name;
    private String appId;
    private List<String> managers;
    private List<String> members;
    private String customId;
    private String departmentId;
    private boolean archived;
    private boolean active;

    public boolean isVisible() {
        return this.active && !this.archived;
    }

    public boolean containsManager(String memberId) {
        return managers.contains(memberId);
    }

    public boolean containsMember(String memberId) {
        return members.contains(memberId);
    }

    public boolean isSynced() {
        return isNotBlank(departmentId);
    }
}
