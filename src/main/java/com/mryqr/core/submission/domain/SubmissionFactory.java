package com.mryqr.core.submission.domain;

import com.mryqr.common.domain.permission.Permission;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.qr.domain.PlatedQr;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrFactory;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.SubmissionDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.mryqr.common.domain.user.User.ANONYMOUS_HUMAN_USER;
import static com.mryqr.common.domain.user.User.NO_USER;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static java.util.function.Function.identity;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
@RequiredArgsConstructor
public class SubmissionFactory {
    private final SubmissionRepository submissionRepository;
    private final QrFactory qrFactory;
    private final SubmissionDomainService submissionDomainService;

    public CreateSubmissionWithQrResult createSubmissionFromQrTemplate(Set<Answer> answers,
                                                                       QR templateQr,
                                                                       Page page,
                                                                       App app,
                                                                       Set<Permission> permissions,
                                                                       String referenceData,
                                                                       User user) {

        User finalUser = page.requireLogin() ? user : ANONYMOUS_HUMAN_USER;//只有需要登录的页面才记录user
        PlatedQr platedQr = qrFactory.createPlatedQrFromTemplate(templateQr, app, finalUser);
        QR qr = platedQr.getQr();

        Map<String, Answer> checkedAnswers = submissionDomainService.checkAnswers(answers, qr, page, app, permissions);
        Submission submission = new Submission(checkedAnswers, page.getId(), qr, app, referenceData, finalUser);

        return CreateSubmissionWithQrResult.builder()
                .submission(submission)
                .qr(qr)
                .plate(platedQr.getPlate())
                .build();
    }

    public Submission createNewSubmission(Set<Answer> answers,
                                          QR qr,
                                          Page page,
                                          App app,
                                          Set<Permission> permissions,
                                          String referenceData,
                                          User user) {
        if (page.isOncePerInstanceSubmitType()) {
            submissionRepository.lastInstanceSubmission(qr.getId(), page.getId())
                    .ifPresent(submission -> {//此时即便存在已有submission，也不允许直接更新，因为希望用户先看到已有的数据在调用更新API，这样保证用户知情已有数据
                        throw new MryException(SUBMISSION_ALREADY_EXISTS_FOR_INSTANCE,
                                "当前页面不支持重复提交，请尝试更新已有表单。",
                                mapOf("qrId", qr.getId(), "pageId", page.getId()));
                    });
        }

        if (page.isOncePerMemberSubmitType()) {
            if (user == null || isBlank(user.getMemberId())) {
                throw new MryException(SUBMISSION_REQUIRE_MEMBER, "未提供提交者ID。", mapOf("pageId", page.getId()));
            }

            submissionRepository.lastMemberSubmission(user.getMemberId(), qr.getId(), page.getId())
                    .ifPresent(submission -> {//此时即便存在已有submission，也不允许直接更新，因为希望用户在前端先看到已有的数据再更新，这样保证用户知情已有数据
                        throw new MryException(SUBMISSION_ALREADY_EXISTS_FOR_MEMBER,
                                "当前页面不支持重复提交，请尝试更新已有表单。",
                                mapOf("qrId", qr.getId(), "pageId", page.getId()));
                    });
        }

        User finalUser = page.requireLogin() ? user : ANONYMOUS_HUMAN_USER;//只有需要登录的页面才记录user
        Map<String, Answer> checkedAnswers = submissionDomainService.checkAnswers(answers, qr, page, app, permissions);
        return new Submission(checkedAnswers, page.getId(), qr, app, referenceData, finalUser);
    }

    public Submission createOrUpdateSubmission(Set<Answer> answers,
                                               QR qr,
                                               Page page,
                                               App app,
                                               Set<Permission> permissions,
                                               String referenceData,
                                               User user) {
        if (page.isOncePerInstanceSubmitType()) {
            Optional<Submission> submissionOptional = submissionRepository.lastInstanceSubmission(qr.getId(), page.getId());
            if (submissionOptional.isPresent()) {
                Submission submission = submissionOptional.get();
                submissionDomainService.updateSubmission(submission, app, page, qr, answers, permissions, user);
                return submission;
            }
        }

        if (page.isOncePerMemberSubmitType()) {
            if (isBlank(user.getMemberId())) {
                throw new MryException(SUBMISSION_REQUIRE_MEMBER, "未提供提交者ID。", mapOf("pageId", page.getId()));
            }

            Optional<Submission> submissionOptional = submissionRepository.lastMemberSubmission(user.getMemberId(), qr.getId(), page.getId());
            if (submissionOptional.isPresent()) {
                Submission submission = submissionOptional.get();
                submissionDomainService.updateSubmission(submission, app, page, qr, answers, permissions, user);
                return submission;
            }
        }

        User finalUser = page.requireLogin() ? user : ANONYMOUS_HUMAN_USER;//只有需要登录的页面才记录user
        Map<String, Answer> checkedAnswers = submissionDomainService.checkAnswers(answers, qr, page, app, permissions);
        return new Submission(checkedAnswers, page.getId(), qr, app, referenceData, finalUser);
    }

    public Submission createImportQrSubmission(Set<Answer> answers, String pageId, QR qr, App app) {
        Map<String, Answer> answerMap = answers.stream().collect(toImmutableMap(Answer::getControlId, identity()));
        return new Submission(answerMap, pageId, qr, app, null, NO_USER);
    }
}
