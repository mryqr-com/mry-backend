package com.mryqr.integration.submission.command;

import com.mryqr.common.domain.permission.Permission;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberRepository;
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

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntegrationSubmissionCommandService {
    private final MryRateLimiter mryRateLimiter;
    private final QrRepository qrRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionFactory submissionFactory;
    private final TenantRepository tenantRepository;
    private final PlateRepository plateRepository;
    private final SubmissionDomainService submissionDomainService;
    private final MemberRepository memberRepository;

    @Transactional
    public String newSubmission(String qrId, IntegrationNewSubmissionCommand command, User user) {
        if (!user.isMryManageTenantUser()) {
            mryRateLimiter.applyFor(user.getTenantId(), "Integration:Submission:Create", 10);
        }

        AppedQr appedQr = qrRepository.appedQrByIdAndCheckTenantShip(qrId, user);
        String pageId = command.getPageId();
        Set<Answer> answers = command.getAnswers();
        String memberId = command.getMemberId();
        String memberCustomId = command.getMemberCustomId();
        String referenceData = command.getReferenceData();

        String submissionId = newSubmission(appedQr, pageId, answers, memberId, memberCustomId, referenceData, user);
        log.info("Integration created submission[{}] for qr[{}].", submissionId, qrId);

        return submissionId;
    }

    @Transactional
    public String newSubmissionByQrCustomId(String appId,
                                            String qrCustomId,
                                            IntegrationNewSubmissionCommand command, User user) {
        if (!user.isMryManageTenantUser()) {
            mryRateLimiter.applyFor(user.getTenantId(), "Integration:Submission:CreateByQrCustomId", 10);
        }

        AppedQr appedQr = qrRepository.appedQrByCustomIdAndCheckTenantShip(appId, qrCustomId, user);
        String pageId = command.getPageId();
        Set<Answer> answers = command.getAnswers();
        String memberId = command.getMemberId();
        String memberCustomId = command.getMemberCustomId();
        String referenceData = command.getReferenceData();
        String submissionId = newSubmission(appedQr, pageId, answers, memberId, memberCustomId, referenceData, user);
        log.info("Integration created submission[{}] by QR custom ID[appId={},customId={}].", submissionId, appId, qrCustomId);
        return submissionId;
    }

    private String newSubmission(AppedQr appedQr,
                                 String pageId,
                                 Set<Answer> answers,
                                 String memberId,
                                 String memberCustomId,
                                 String referenceData,
                                 User user) {
        App app = appedQr.getApp();
        QR qr = appedQr.getQr();
        app.checkActive();
        qr.checkActive(app);

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(app.getTenantId());
        packagesStatus.validateAddSubmission();

        Page page = app.pageById(pageId);
        User memberUser = deriveUser(user, memberId, memberCustomId);

        if (qr.isTemplate()) {
            packagesStatus.validateAddQr();
            packagesStatus.validateAddPlate();
            CreateSubmissionWithQrResult result = submissionFactory.createSubmissionFromQrTemplate(answers,
                    qr,
                    page,
                    app,
                    Set.of(Permission.values()),
                    referenceData,
                    memberUser
            );
            submissionRepository.houseKeepSave(result.getSubmission(), app);
            qrRepository.save(result.getQr());
            plateRepository.save(result.getPlate());
            return result.getSubmission().getId();
        } else {
            Submission submission = submissionFactory.createOrUpdateSubmission(
                    answers,
                    qr,
                    page,
                    app,
                    Set.of(Permission.values()),
                    referenceData,
                    memberUser
            );

            submissionRepository.houseKeepSave(submission, app);
            return submission.getId();
        }
    }

    @Transactional
    public void updateSubmission(String submissionId, IntegrationUpdateSubmissionCommand command, User user) {
        if (!user.isMryManageTenantUser()) {
            mryRateLimiter.applyFor(user.getTenantId(), "Integration:Submission:Update", 10);
        }

        Submission submission = submissionRepository.byIdAndCheckTenantShip(submissionId, user);
        AppedQr appedQr = qrRepository.appedQrById(submission.getQrId());
        App app = appedQr.getApp();
        QR qr = appedQr.getQr();
        app.checkActive();
        qr.checkActive(app);

        Page page = app.pageById(submission.getPageId());
        Set<Answer> answers = command.getAnswers();

        Map<String, Answer> checkedAnswers = submissionDomainService.checkAnswers(answers,
                qr,
                page,
                app,
                submissionId,
                Set.of(Permission.values()));

        User finalUser = deriveUser(user, command.getMemberId(), command.getMemberCustomId());
        Set<String> submittedControlIds = answers.stream().map(Answer::getControlId).collect(toImmutableSet());
        submission.update(submittedControlIds, checkedAnswers, finalUser);
        submissionRepository.houseKeepSave(submission, app);
        log.info("Integration updated submission[{}].", submissionId);
    }

    private User deriveUser(User user, String memberId, String memberCustomId) {
        if (isNotBlank(memberId)) {
            Member member = memberRepository.cachedByIdAndCheckTenantShip(memberId, user);
            return member.toUser();
        } else if (isNotBlank(memberCustomId)) {
            Member member = memberRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), memberCustomId, user);
            return member.toUser();
        }
        return user;
    }

    @Transactional
    public void deleteSubmission(String submissionId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Submission:Delete", 10);

        Submission submission = submissionRepository.byIdAndCheckTenantShip(submissionId, user);
        submission.onDelete(user);
        submissionRepository.delete(submission);
        log.info("Integration deleted submission[{}].", submissionId);
    }
}
