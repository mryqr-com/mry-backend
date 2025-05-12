package com.mryqr.core.submission.domain.answer.mobilenumber;

import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.FMobileNumberControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.SubmissionRepository;
import com.mryqr.core.submission.domain.answer.AbstractSubmissionAnswerChecker;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.common.exception.ErrorCode.ANSWER_NOT_UNIQUE_PER_APP;
import static com.mryqr.common.exception.ErrorCode.ANSWER_NOT_UNIQUE_PER_INSTANCE;
import static com.mryqr.core.app.domain.page.control.AnswerUniqueType.UNIQUE_PER_APP;
import static com.mryqr.core.app.domain.page.control.AnswerUniqueType.UNIQUE_PER_INSTANCE;
import static com.mryqr.core.app.domain.page.control.ControlType.MOBILE;

@Component
@RequiredArgsConstructor
public class MobileNumberAnswerChecker extends AbstractSubmissionAnswerChecker {
    private final SubmissionRepository submissionRepository;

    @Override
    public ControlType controlType() {
        return MOBILE;
    }

    @Override
    protected Answer doCheckAnswer(Answer answer, Control control, QR qr, Page page, App app, String submissionId) {
        FMobileNumberControl theControl = (FMobileNumberControl) control;
        MobileNumberAnswer theAnswer = (MobileNumberAnswer) answer;
        MobileNumberAnswer checkedAnswer = theControl.check(theAnswer);

        if (theControl.getUniqueType() == UNIQUE_PER_APP) {
            IndexedField indexedField = app.indexedFieldForControl(page.getId(), control.getId());
            if (submissionRepository.alreadyExistsForAnswerUnderApp(theAnswer.getMobileNumber(),
                    indexedField,
                    app.getId(),
                    page.getId(),
                    submissionId)) {
                failAnswerValidation(ANSWER_NOT_UNIQUE_PER_APP, control, "手机号已被占用：[" + theAnswer.getMobileNumber() + "]。");
            }
        }

        if (theControl.getUniqueType() == UNIQUE_PER_INSTANCE) {
            IndexedField indexedField = app.indexedFieldForControl(page.getId(), control.getId());
            if (submissionRepository.alreadyExistsForAnswerUnderQr(theAnswer.getMobileNumber(),
                    indexedField,
                    page.getId(),
                    qr.getId(),
                    submissionId)) {
                failAnswerValidation(ANSWER_NOT_UNIQUE_PER_INSTANCE, control, "手机号已被占用：[" + theAnswer.getMobileNumber() + "]。");
            }
        }

        return checkedAnswer;
    }
}
