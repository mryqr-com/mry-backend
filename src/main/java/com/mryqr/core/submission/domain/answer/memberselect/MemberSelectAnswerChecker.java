package com.mryqr.core.submission.domain.answer.memberselect;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.FMemberSelectControl;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.answer.AbstractSubmissionAnswerChecker;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.common.exception.ErrorCode.NOT_ALL_MEMBERS_EXIST;
import static com.mryqr.core.app.domain.page.control.ControlType.MEMBER_SELECT;

@Component
@RequiredArgsConstructor
public class MemberSelectAnswerChecker extends AbstractSubmissionAnswerChecker {
    private final MemberRepository memberRepository;

    @Override
    public ControlType controlType() {
        return MEMBER_SELECT;
    }

    @Override
    protected Answer doCheckAnswer(Answer answer, Control control, QR qr, Page page, App app, String submissionId) {
        MemberSelectAnswer theAnswer = (MemberSelectAnswer) answer;
        MemberSelectAnswer checkedAnswer = ((FMemberSelectControl) control).check(theAnswer);

        if (memberRepository.cachedNotAllMembersExist(theAnswer.getMemberIds(), app.getTenantId())) {
            failAnswerValidation(NOT_ALL_MEMBERS_EXIST, control, "有成员不存在:[" + control.getName() + "]。");
        }

        return checkedAnswer;
    }
}
