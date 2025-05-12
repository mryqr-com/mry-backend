package com.mryqr.management.crm.webhook;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.password.MryPasswordEncoder;
import com.mryqr.common.webhook.submission.BaseSubmissionWebhookPayload;
import com.mryqr.common.webhook.submission.SubmissionCreatedWebhookPayload;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.mryqr.common.utils.CommonUtils.maskMobileOrEmail;
import static com.mryqr.management.crm.MryTenantManageApp.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResetMemberPasswordWebhookHandler implements TenantWebhookHandler {
    private final MemberRepository memberRepository;
    private final MryPasswordEncoder mryPasswordEncoder;

    @Override
    public boolean canHandle(BaseSubmissionWebhookPayload payload) {
        return payload instanceof SubmissionCreatedWebhookPayload && payload.getPageId().equals(RESET_MEMBER_PASSWORD_PAGE_ID);
    }

    @Override
    public void handle(BaseSubmissionWebhookPayload payload) {
        String tenantId = payload.getQrCustomId();

        Map<String, Answer> answers = payload.allAnswers();
        SingleLineTextAnswer mobileOrEmailAnswer = (SingleLineTextAnswer) answers.get(RESET_MEMBER_PASSWORD_MOBILE_OR_EMAIL_CONTROL_ID);
        SingleLineTextAnswer passwordAnswer = (SingleLineTextAnswer) answers.get(RESET_MEMBER_PASSWORD_PASSWORD_CONTROL_ID);
        String mobileOrEmail = mobileOrEmailAnswer.getContent();

        this.memberRepository.byMobileOrEmailOptional(mobileOrEmail).ifPresent(member -> {
            if (Objects.equals(member.getTenantId(), tenantId)) {
                member.changePassword(mryPasswordEncoder.encode(passwordAnswer.getContent()), User.NO_USER);
                memberRepository.save(member);
                log.info("Reset password for member[{}]-[{}].", member.getId(), maskMobileOrEmail(mobileOrEmail));
            }
        });
    }
}
