package com.mryqr.core.common.domain.permission;

import java.util.Arrays;

import static java.util.Comparator.comparing;

//用户针对某个QR的相关权限
public enum Permission {
    PUBLIC(0),//公开
    AS_TENANT_MEMBER(1),//QR所在租户的成员
    AS_GROUP_MEMBER(2),//QR所在group的成员
    CAN_MANAGE_GROUP(3),//QR所在group的管理者
    CAN_MANAGE_APP(4);//QR所在App的管理者

    private final int tier;//权限级别，大者自动拥有小者的权限

    Permission(int tier) {
        this.tier = tier;
    }

    public boolean covers(Permission permission) {
        return this.tier >= permission.tier;
    }

    public int getTier() {
        return tier;
    }

    public static Permission maxPermission(Permission... permissions) {
        return Arrays.stream(permissions).max(comparing(Permission::getTier)).orElse(PUBLIC);
    }

    public static Permission minPermission(Permission... permissions) {
        return Arrays.stream(permissions).min(comparing(Permission::getTier)).orElse(PUBLIC);
    }

    public boolean isPublic() {
        return this == PUBLIC;
    }

    public boolean requireLogin() {
        return !isPublic();
    }
}
