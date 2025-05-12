package com.mryqr.core.common.domain.permission;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.qr.domain.QR;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Set;

import static com.mryqr.core.common.exception.ErrorCode.NO_APPROVABLE_GROUPS;
import static com.mryqr.core.common.exception.ErrorCode.NO_APPROVABLE_PAGES;
import static com.mryqr.core.common.exception.ErrorCode.NO_APPROVABLE_PERMISSION_FOR_GROUP;
import static com.mryqr.core.common.exception.ErrorCode.NO_APPROVABLE_PERMISSION_FOR_PAGE;
import static com.mryqr.core.common.exception.ErrorCode.NO_APPROVABLE_PERMISSION_FOR_QR;
import static com.mryqr.core.common.exception.ErrorCode.NO_MANAGABLE_GROUPS;
import static com.mryqr.core.common.exception.ErrorCode.NO_MANAGABLE_PAGES;
import static com.mryqr.core.common.exception.ErrorCode.NO_MANAGABLE_PERMISSION_FOR_GROUP;
import static com.mryqr.core.common.exception.ErrorCode.NO_MANAGABLE_PERMISSION_FOR_PAGE;
import static com.mryqr.core.common.exception.ErrorCode.NO_MANAGABLE_PERMISSION_FOR_QR;
import static com.mryqr.core.common.exception.ErrorCode.NO_VIEWABLE_GROUPS;
import static com.mryqr.core.common.exception.ErrorCode.NO_VIEWABLE_PAGES;
import static com.mryqr.core.common.exception.ErrorCode.NO_VIEWABLE_PERMISSION_FOR_GROUP;
import static com.mryqr.core.common.exception.ErrorCode.NO_VIEWABLE_PERMISSION_FOR_PAGE;
import static com.mryqr.core.common.exception.ErrorCode.NO_VIEWABLE_PERMISSION_FOR_QR;
import static com.mryqr.core.common.exception.MryException.accessDeniedException;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class AppOperatePermissions {
    private final User user;
    private final String appId;
    private final boolean canManageApp;
    private final Map<String, String> groupFullNames;

    private final Set<String> viewableGroupIds;//可以查看的group（用于qr列表，提交者提交列表等处）
    private final Set<String> viewablePageIds;//可以查看的页面（用于提交者提交列表等处）

    private final Set<String> managableGroupIds;//可以管理的group
    private final Set<String> managablePageIds;//可以管理的页面（用于所有提交列表等处）

    private final Set<String> approvableGroupIds;//可以审批的group
    private final Set<String> approvablePageIds;//可以审批的页面

    public void checkHasPermissions() {
        if (this.getViewableGroupIds().isEmpty()) {
            throw accessDeniedException();
        }
    }

    public void checkHasViewableGroups() {
        if (isEmpty(viewableGroupIds)) {
            throw new MryException(NO_VIEWABLE_GROUPS, "无可查看分组。", mapOf("appId", appId));
        }
    }

    public void checkViewableGroupPermission(String groupId) {
        if (!viewableGroupIds.contains(groupId)) {
            throw new MryException(NO_VIEWABLE_PERMISSION_FOR_GROUP, "对分组无查看权限。", mapOf("appId", appId, "groupId", groupId));
        }
    }

    public void checkViewableGroupPermission(QR qr) {
        if (!viewableGroupIds.contains(qr.getGroupId())) {
            throw new MryException(NO_VIEWABLE_PERMISSION_FOR_QR, "对实例无查看权限。", mapOf("appId", appId, "qrId", qr.getId()));
        }
    }

    public void checkHasViewablePages() {
        if (isEmpty(viewablePageIds)) {
            throw new MryException(NO_VIEWABLE_PAGES, "无页面可查看。", mapOf("appId", appId));

        }
    }

    public void checkViewablePagePermission(String pageId) {
        if (!viewablePageIds.contains(pageId)) {
            throw new MryException(NO_VIEWABLE_PERMISSION_FOR_PAGE, "对页面无查看权限。", mapOf("appId", appId, "pageId", pageId));
        }
    }

    public void checkHasManagableGroups() {
        if (isEmpty(managableGroupIds)) {
            throw new MryException(NO_MANAGABLE_GROUPS, "无可管理分组。", mapOf("appId", appId));
        }
    }

    public void checkManagableGroupPermission(String groupId) {
        if (!managableGroupIds.contains(groupId)) {
            throw new MryException(NO_MANAGABLE_PERMISSION_FOR_GROUP, "对分组无管理权限。", mapOf("groupId", groupId));
        }
    }

    public void checkManagableGroupPermission(QR qr) {
        if (!managableGroupIds.contains(qr.getGroupId())) {
            throw new MryException(NO_MANAGABLE_PERMISSION_FOR_QR, "对实例无管理权限。", mapOf("appId", appId, "qrId", qr.getId()));
        }
    }

    public void checkHasManagablePages() {
        if (isEmpty(managablePageIds)) {
            throw new MryException(NO_MANAGABLE_PAGES, "无页面可管理。", mapOf("appId", appId));
        }
    }

    public void checkManagablePagePermission(String pageId) {
        if (!managablePageIds.contains(pageId)) {
            throw new MryException(NO_MANAGABLE_PERMISSION_FOR_PAGE, "对页面无管理权限。", mapOf("pageId", pageId));
        }
    }

    public void checkHasApprovableGroups() {
        if (isEmpty(approvableGroupIds)) {
            throw new MryException(NO_APPROVABLE_GROUPS, "无可审批分组。", mapOf("appId", appId));
        }
    }

    public void checkApprovableGroupPermission(String groupId) {
        if (!approvableGroupIds.contains(groupId)) {
            throw new MryException(NO_APPROVABLE_PERMISSION_FOR_GROUP, "对分组无审批权限。", mapOf("groupId", groupId));
        }
    }

    public void checkApprovableGroupPermission(QR qr) {
        if (!approvableGroupIds.contains(qr.getGroupId())) {
            throw new MryException(NO_APPROVABLE_PERMISSION_FOR_QR, "对实例无审批权限。", mapOf("qrId", qr.getId()));
        }
    }

    public void checkHasApprovablePages() {
        if (isEmpty(approvablePageIds)) {
            throw new MryException(NO_APPROVABLE_PAGES, "无可审批页面。", mapOf("appId", appId));
        }
    }

    public void checkApprovablePagePermission(String pageId) {
        if (!approvablePageIds.contains(pageId)) {
            throw new MryException(NO_APPROVABLE_PERMISSION_FOR_PAGE, "对页面无审批权限。", mapOf("pageId", pageId));
        }
    }

    public boolean canManageGroup(String groupId) {
        return managableGroupIds.contains(groupId);
    }
}
