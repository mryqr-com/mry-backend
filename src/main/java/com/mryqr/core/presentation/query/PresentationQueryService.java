package com.mryqr.core.presentation.query;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.common.domain.permission.SubmissionPermissionChecker;
import com.mryqr.core.common.domain.permission.SubmissionPermissions;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.qr.domain.AppedQr;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.mryqr.core.app.domain.page.control.ControlType.BAR;
import static com.mryqr.core.app.domain.page.control.ControlType.DOUGHNUT;
import static com.mryqr.core.app.domain.page.control.ControlType.NUMBER_RANGE_SEGMENT;
import static com.mryqr.core.app.domain.page.control.ControlType.PIE;
import static com.mryqr.core.app.domain.page.control.ControlType.TIME_SEGMENT;
import static com.mryqr.core.app.domain.page.control.ControlType.TREND;
import static com.mryqr.core.common.domain.permission.Permission.maxPermission;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_NOT_COMPLETE;
import static com.mryqr.core.common.exception.MryException.accessDeniedException;
import static com.mryqr.core.common.exception.MryException.authenticationException;
import static com.mryqr.core.common.utils.MapUtils.mapOf;

@Component
@RequiredArgsConstructor
public class PresentationQueryService {
    private final Set<ControlType> LOGIN_REQUIRED_CONTROL_TYPES = Set.of(TREND, BAR, PIE, DOUGHNUT, TIME_SEGMENT, NUMBER_RANGE_SEGMENT);
    private final List<ControlPresentationer> presentationers;
    private final QrRepository qrRepository;
    private final SubmissionPermissionChecker submissionPermissionChecker;
    private final MryRateLimiter mryRateLimiter;
    private final TenantRepository tenantRepository;

    public QControlPresentation fetchPresentation(String qrId, String pageId, String controlId, User user) {
        AppedQr appedQr = qrRepository.appedQrById(qrId);
        App app = appedQr.getApp();
        mryRateLimiter.applyFor(app.getTenantId(), "Presentation:Fetch", 20);

        QR qr = appedQr.getQr();
        Page page = app.pageById(pageId);
        Control control = page.controlById(controlId);

        if (LOGIN_REQUIRED_CONTROL_TYPES.contains(control.getType())) {
            if (!user.isLoggedIn()) {
                throw authenticationException();
            }

            if (!user.isLoggedInFor(app.getTenantId())) {
                throw accessDeniedException("控件需要所属租户下的成员登录后才可查看。");
            }

            Tenant tenant = tenantRepository.cachedById(app.getTenantId());
            tenant.packagesStatus().validateControlType(control.getType());
        }

        SubmissionPermissions submissionPermissions = submissionPermissionChecker.permissionsFor(user, appedQr);
        if (maxPermission(app.requiredPermission(), page.requiredPermission(), control.requiredPermission()).requireLogin()) {
            user.checkIsLoggedIn();
        }

        submissionPermissions.checkPermissions(app.requiredPermission(), page.requiredPermission(), control.requiredPermission());

        if (!control.isComplete()) {
            throw new MryException(CONTROL_NOT_COMPLETE, "控件不完整。", mapOf("controlId", control.getId()));
        }

        return presentationers.stream().filter(presentationer -> presentationer.canHandle(control)).findFirst()
                .map(presentationer -> presentationer.present(qr, control, app))
                .orElseThrow(() -> new NoSuchElementException("No presentationer found."));
    }
}
