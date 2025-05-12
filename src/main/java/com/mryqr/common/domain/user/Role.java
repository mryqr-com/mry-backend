package com.mryqr.common.domain.user;

public enum Role {
    TENANT_ADMIN("系统管理员"),
    TENANT_MEMBER("普通成员");

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
