package com.mryqr.core.submission.command;

import com.mryqr.common.domain.permission.ManagePermissionChecker;
import com.mryqr.common.domain.permission.SubmissionPermissionChecker;
import com.mryqr.common.domain.permission.SubmissionPermissions;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.qr.domain.AppedQr;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.submission.domain.CreateSubmissionWithQrResult;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.SubmissionFactory;
import com.mryqr.core.submission.domain.SubmissionRepository;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.SubmissionDomainService;
import com.mryqr.core.tenant.domain.PackagesStatus;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.mryqr.common.domain.permission.Permission.maxPermission;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionCommandService {
    private final QrRepository qrRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionFactory submissionFactory;
    private final SubmissionPermissionChecker submissionPermissionChecker;
    private final ManagePermissionChecker managePermissionChecker;
    private final AppRepository appRepository;
    private final PlateRepository plateRepository;
    private final SubmissionDomainService submissionDomainService;
    private final TenantRepository tenantRepository;
    private final GroupRepository groupRepository;
    private final MryRateLimiter mryRateLimiter;

    @Transactional
    public String newSubmission(NewSubmissionCommand command, User user) {
        AppedQr appedQr = qrRepository.appedQrById(command.getQrId());
        App app = appedQr.getApp();
        mryRateLimiter.applyFor(app.getTenantId(), "Submission:New", 50);

        QR qr = appedQr.getQr();
        app.checkActive();
        qr.checkActive(app);

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(app.getTenantId());
        packagesStatus.validateAddSubmission();

        Page page = app.pageById(command.getPageId());
        SubmissionPermissions submissionPermissions = submissionPermissionChecker.permissionsFor(user, appedQr);

        if (maxPermission(app.requiredPermission(), page.requiredPermission()).requireLogin()) {
            user.checkIsHumanUser();
        }

        submissionPermissions.checkPermissions(app.requiredPermission(), page.requiredPermission());
        Set<Answer> answers = command.getAnswers();

        if (qr.isTemplate()) {
            packagesStatus.validateAddQr();
            packagesStatus.validateAddPlate();
            CreateSubmissionWithQrResult result = submissionFactory.createSubmissionFromQrTemplate(answers,
                    qr,
                    page,
                    app,
                    submissionPermissions.getPermissions(),
                    command.getReferenceData(),
                    user
            );

            submissionRepository.houseKeepSave(result.getSubmission(), app);
            qrRepository.save(result.getQr());
            plateRepository.save(result.getPlate());
            return result.getSubmission().getId();
        } else {
            Submission submission = submissionFactory.createNewSubmission(
                    answers,
                    qr,
                    page,
                    app,
                    submissionPermissions.getPermissions(),
                    command.getReferenceData(),
                    user
            );

            submissionRepository.houseKeepSave(submission, app);
            log.info("Created submission[{}] for app[{}].", submission.getId(), app.getId());

            return submission.getId();
        }
    }

    @Transactional
    public void updateSubmission(String submissionId, UpdateSubmissionCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Submission:Update", 20);

        Submission submission = submissionRepository.byIdAndCheckTenantShip(submissionId, user);
        AppedQr appedQr = qrRepository.appedQrById(submission.getQrId());
        App app = appedQr.getApp();
        QR qr = appedQr.getQr();
        app.checkActive();
        qr.checkActive(app);

        Page page = app.pageById(submission.getPageId());
        SubmissionPermissions submissionPermissions = submissionPermissionChecker.permissionsFor(user, app, submission.getGroupId());
        submissionPermissions.checkCanUpdateSubmission(submission, page, app);

        submissionDomainService.updateSubmission(submission,
                app,
                page,
                qr,
                command.getAnswers(),
                submissionPermissions.getPermissions(),
                user
        );

        submissionRepository.houseKeepSave(submission, app);
        log.info("Updated submission[{}] for app[{}].", submissionId, app.getId());
    }

    @Transactional
    public void approveSubmission(String submissionId, ApproveSubmissionCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Submission:Approve", 20);

        Submission submission = submissionRepository.byIdAndCheckTenantShip(submissionId, user);
        PackagesStatus packagesStatus = tenantRepository.cachedById(submission.getTenantId()).packagesStatus();
        packagesStatus.validateApproveSubmission();

        QR qr = qrRepository.byId(submission.getQrId());
        App app = appRepository.cachedById(submission.getAppId());
        app.checkActive();
        qr.checkActive(app);

        Page page = app.pageById(submission.getPageId());
        SubmissionPermissions submissionPermissions = submissionPermissionChecker.permissionsFor(user, app, submission.getGroupId());
        submissionPermissions.checkCanApproveSubmission(submission, page, app);

        submission.approve(command.isPassed(), command.getNote(), page, user);
        submissionRepository.houseKeepSave(submission, app);
        log.info("Approved submission[{}] for app[{}].", submissionId, app.getId());
    }

    @Transactional
    public void deleteSubmission(String submissionId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Submission:Delete", 10);

        Submission submission = submissionRepository.byIdAndCheckTenantShip(submissionId, user);
        Group group = groupRepository.cachedById(submission.getGroupId());
        managePermissionChecker.checkCanManageGroup(user, group);

        submission.onDelete(user);
        submissionRepository.delete(submission);
        log.info("Deleted submission[{}] for app[{}].", submissionId, submission.getAppId());
    }
}
