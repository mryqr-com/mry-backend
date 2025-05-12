package com.mryqr.common.oss.command;

import com.mryqr.common.domain.permission.ManagePermissionChecker;
import com.mryqr.common.domain.permission.SubmissionPermissionChecker;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.oss.domain.AliyunOssTokenGenerator;
import com.mryqr.common.oss.domain.QOssToken;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.qr.domain.AppedQr;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.tenant.domain.PackagesStatus;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.mryqr.common.domain.permission.Permission.maxPermission;
import static com.mryqr.common.exception.ErrorCode.QR_NOT_BELONG_TO_APP;
import static com.mryqr.common.exception.ErrorCode.USER_NOT_CURRENT_MEMBER;

@Component
@RequiredArgsConstructor
public class OssTokenCommandService {
    private final AliyunOssTokenGenerator ossTokenGenerator;
    private final ManagePermissionChecker managePermissionChecker;
    private final QrRepository qrRepository;
    private final AppRepository appRepository;
    private final SubmissionPermissionChecker submissionPermissionChecker;
    private final MryRateLimiter mryRateLimiter;
    private final TenantRepository tenantRepository;

    public QOssToken generateOssToken(RequestOssTokenCommand command, User user) {
        String tenantId = command.getTenantId();

        switch (command.getType()) {
            case TENANT_EDIT -> {
                mryRateLimiter.applyFor(tenantId, "OssToken:TenantEdit", 5);
                user.checkIsHumanUser();
                user.checkIsTenantAdminFor(tenantId);
            }

            case TENANT_ORDER -> {
                mryRateLimiter.applyFor(tenantId, "OssToken:TenantOrder", 5);
                user.checkIsHumanUser();
                user.checkIsTenantAdminFor(tenantId);
            }

            case APP_EDIT -> {
                mryRateLimiter.applyFor(tenantId, "OssToken:AppEdit", 5);
                user.checkIsHumanUser();
                user.checkIsLoggedInFor(tenantId);
                App app = appRepository.cachedByIdAndCheckTenantShip(command.getAppId(), user);
                managePermissionChecker.checkCanManageApp(user, app);
            }

            case QR_MANAGE -> {
                mryRateLimiter.applyFor(tenantId, "OssToken:QrManage", 10);
                user.checkIsHumanUser();
                user.checkIsLoggedInFor(tenantId);
                QR qr = qrRepository.byIdAndCheckTenantShip(command.getQrId(), user);

                if (!Objects.equals(qr.getAppId(), command.getAppId())) {
                    throw new MryException(QR_NOT_BELONG_TO_APP, "QR的appId与所提供的appId不一致。",
                            "qrAppId", qr.getAppId(), "appId", command.getAppId());
                }

                managePermissionChecker.checkCanManageQr(user, qr);
            }

            case SUBMISSION -> {
                AppedQr appedQr = qrRepository.appedQrById(command.getQrId());
                App app = appedQr.getApp();
                tenantId = app.getTenantId();

                if (!Objects.equals(app.getId(), command.getAppId())) {
                    throw new MryException(QR_NOT_BELONG_TO_APP, "QR的appId与所提供的appId不一致。",
                            "qrAppId", app.getId(), "appId", command.getAppId());
                }

                mryRateLimiter.applyFor(app.getTenantId(), "OssToken:Submission", 50);
                Page page = app.pageById(command.getPageId());
                if (maxPermission(app.requiredPermission(), page.requiredPermission()).requireLogin()) {
                    user.checkIsHumanUser();
                }

                submissionPermissionChecker.checkPermissions(user, appedQr, app.requiredPermission(), page.requiredPermission());
            }

            case MEMBER_INFO -> {
                user.checkIsHumanUser();
                user.checkIsLoggedInFor(tenantId);
                if (!Objects.equals(user.getMemberId(), command.getMemberId())) {
                    throw new MryException(USER_NOT_CURRENT_MEMBER, "所提供用户ID与当前用户ID不一致。",
                            "memberId", command.getMemberId(), "currentMemberId", user.getMemberId());
                }
                mryRateLimiter.applyFor(tenantId, "OssToken:MemberInfo", 10);
            }
        }

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(tenantId);
        packagesStatus.validateRequestOssToken();
        return ossTokenGenerator.generateOssToken(command.folder());
    }

}
