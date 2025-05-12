package com.mryqr.core.common.domain.permission;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.ErrorCode;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.submission.domain.Submission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Objects;
import java.util.Set;

import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_GROUP;
import static com.mryqr.core.common.domain.permission.Permission.maxPermission;
import static com.mryqr.core.common.domain.permission.SubmissionPermissions.CheckResult.failure;
import static com.mryqr.core.common.domain.permission.SubmissionPermissions.CheckResult.success;
import static com.mryqr.core.common.exception.ErrorCode.ACCESS_DENIED;
import static com.mryqr.core.common.exception.ErrorCode.APPROVAL_NOT_ENABLED;
import static com.mryqr.core.common.exception.ErrorCode.CANNOT_UPDATE_APPROVED_SUBMISSION;
import static com.mryqr.core.common.exception.ErrorCode.NO_APPROVABLE_PAGES;
import static com.mryqr.core.common.exception.ErrorCode.NO_APPROVABLE_PERMISSION_FOR_PAGE;
import static com.mryqr.core.common.exception.ErrorCode.NO_MANAGABLE_PAGES;
import static com.mryqr.core.common.exception.ErrorCode.NO_MANAGABLE_PERMISSION_FOR_PAGE;
import static com.mryqr.core.common.exception.ErrorCode.NO_VIEWABLE_PAGES;
import static com.mryqr.core.common.exception.ErrorCode.NO_VIEWABLE_PERMISSION_FOR_PAGE;
import static com.mryqr.core.common.exception.ErrorCode.PAGE_NOT_ALLOW_CHANGE_BY_SUBMITTER;
import static com.mryqr.core.common.exception.ErrorCode.SUBMISSION_ALREADY_APPROVED;
import static com.mryqr.core.common.exception.ErrorCode.UPDATE_PERIOD_EXPIRED;
import static com.mryqr.core.common.exception.MryException.accessDeniedException;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class SubmissionPermissions {
    private final User user;
    private final Set<Permission> permissions;

    private final Set<String> canViewFillablePageIds;
    private final Set<String> canManageFillablePageIds;
    private final Set<String> canApproveFillablePageIds;

    public void checkPermission(Permission permission) {
        if (permission.isPublic()) {
            return;
        }

        user.checkIsLoggedIn();

        if (!hasPermission(permission)) {
            throw accessDeniedException();
        }
    }

    public void checkPermissions(Permission... permissions) {
        checkPermission(maxPermission(permissions));
    }

    public boolean hasPermission(Permission permission) {
        return this.permissions.contains(permission);
    }

    public boolean hasPermissions(Permission... permissions) {
        return hasPermission(maxPermission(permissions));
    }

    public boolean hasManageQrPermission() {
        return this.permissions.contains(CAN_MANAGE_APP) || this.permissions.contains(CAN_MANAGE_GROUP);
    }

    public boolean hasManageAppPermission() {
        return this.permissions.contains(CAN_MANAGE_APP);
    }

    public boolean isSubmittedByCurrentUser(Submission submission) {
        return user.isHumanUser() && Objects.equals(user.getMemberId(), submission.getCreatedBy());
    }

    public CheckResult canUpdateSubmission(Submission submission, Page page, App app) {

        //基本权限都不够，则不行
        if (!hasPermissions(app.requiredPermission(), page.requiredPermission())) {
            return failure(ACCESS_DENIED, "权限不足。");
        }

        //如果对页面有更新权限，则成功
        if (hasPermission(page.requiredModifyPermission())) {
            return success();
        }

        //非当前用户提交，不行
        if (!isSubmittedByCurrentUser(submission)) {
            return failure(ACCESS_DENIED, "权限不足，非本人提交。");
        }

        //是当前用户提交但是未启动提交者更新，不行
        if (!page.isSubmitterUpdatable()) {
            return failure(PAGE_NOT_ALLOW_CHANGE_BY_SUBMITTER, "无法更新，页面不允许提交后更新。");
        }

        //是当前用户提交但是已经审批过了，不行
        if (submission.isApproved()) {
            return failure(CANNOT_UPDATE_APPROVED_SUBMISSION, "无法更新，审批之后的提交不允许再次更新。");
        }

        //是当前用户提交但是已经过了可更新期限，不行
        if (!page.submitterUpdateRange().validFrom(submission.getCreatedAt())) {
            return failure(UPDATE_PERIOD_EXPIRED, "无法更新，已经超过可更新时限。");
        }

        //当前用户，启用更新，未审批，在更新期限之内，好，可以
        return success();
    }

    public void checkCanUpdateSubmission(Submission submission, Page page, App app) {
        user.checkIsLoggedInFor(app.getTenantId());

        CheckResult checkResult = canUpdateSubmission(submission, page, app);
        if (checkResult.isFailed()) {
            throw new MryException(checkResult.getErrorCode(), checkResult.getMessage(), mapOf("submissionId", submission.getId()));
        }
    }

    public CheckResult canViewSubmission(Submission submission, Page page, App app) {
        if (!hasPermissions(app.requiredPermission(), page.requiredPermission())) {
            return failure(ACCESS_DENIED, "权限不足。");
        }

        //group管理员及以上任何时候都可以查看
        if (hasManageQrPermission()) {
            return success();
        }

        //自己提交的表单，自己任何时候都可以看到
        if (isSubmittedByCurrentUser(submission)) {
            return success();
        }

        return failure(ACCESS_DENIED, "权限不足。");
    }

    public void checkCanViewSubmission(Submission submission, Page page, App app) {
        user.checkIsLoggedInFor(app.getTenantId());

        CheckResult checkResult = canViewSubmission(submission, page, app);
        if (checkResult.isFailed()) {
            throw new MryException(checkResult.getErrorCode(), checkResult.getMessage(), mapOf("submissionId", submission.getId()));
        }
    }

    public CheckResult canApproveSubmission(Submission submission, Page page, App app) {
        if (!hasPermissions(app.requiredPermission(), page.requiredPermission(), page.requiredApprovalPermission())) {
            return failure(ACCESS_DENIED, "权限不足。");
        }

        if (!page.isApprovalEnabled()) {
            return failure(APPROVAL_NOT_ENABLED, "无法完成审批，未启用审批功能。");
        }

        if (submission.isApproved()) {
            return failure(SUBMISSION_ALREADY_APPROVED, "无法完成审批，先前已经完成审批。");
        }

        return success();
    }

    public void checkCanApproveSubmission(Submission submission, Page page, App app) {
        user.checkIsLoggedInFor(app.getTenantId());

        SubmissionPermissions.CheckResult checkResult = canApproveSubmission(submission, page, app);
        if (checkResult.isFailed()) {
            throw new MryException(checkResult.getErrorCode(), checkResult.getMessage(), mapOf("submissionId", submission.getId()));
        }
    }

    public void checkHasManagablePages() {
        if (isEmpty(canManageFillablePageIds)) {
            throw new MryException(NO_MANAGABLE_PAGES, "无页面可管理。", mapOf("memberId", user.getMemberId()));
        }
    }

    public void checkManagablePagePermission(String pageId) {
        if (!canManageFillablePageIds.contains(pageId)) {
            throw new MryException(NO_MANAGABLE_PERMISSION_FOR_PAGE, "对页面无管理权限。", mapOf("pageId", pageId));
        }
    }

    public void checkHasApprovablePages() {
        if (isEmpty(canApproveFillablePageIds)) {
            throw new MryException(NO_APPROVABLE_PAGES, "无页面可审批。", mapOf("memberId", user.getMemberId()));
        }
    }

    public void checkApprovablePagePermission(String pageId) {
        if (!canApproveFillablePageIds.contains(pageId)) {
            throw new MryException(NO_APPROVABLE_PERMISSION_FOR_PAGE, "对页面无审批权限。", mapOf("pageId", pageId));
        }
    }

    public void checkHasViewablePages() {
        if (isEmpty(canViewFillablePageIds)) {
            throw new MryException(NO_VIEWABLE_PAGES, "无页面可查看。", mapOf("memberId", user.getMemberId()));
        }
    }

    public void checkViewablePagePermission(String pageId) {
        if (!canViewFillablePageIds.contains(pageId)) {
            throw new MryException(NO_VIEWABLE_PERMISSION_FOR_PAGE, "对页面无查看权限。", mapOf("pageId", pageId));
        }
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class CheckResult {
        boolean success;
        ErrorCode errorCode;
        String message;

        public static CheckResult success() {
            return CheckResult.builder().success(true).build();
        }

        public static CheckResult failure(ErrorCode errorCode, String message) {
            return CheckResult.builder().success(false).errorCode(errorCode).message(message).build();
        }

        public boolean isFailed() {
            return !success;
        }

    }
}
