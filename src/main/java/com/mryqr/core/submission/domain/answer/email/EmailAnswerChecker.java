package com.mryqr.core.submission.domain.answer.email;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.FEmailControl;
import com.mryqr.core.common.domain.indexedfield.IndexedField;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.SubmissionRepository;
import com.mryqr.core.submission.domain.answer.AbstractSubmissionAnswerChecker;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.page.control.AnswerUniqueType.UNIQUE_PER_APP;
import static com.mryqr.core.app.domain.page.control.AnswerUniqueType.UNIQUE_PER_INSTANCE;
import static com.mryqr.core.app.domain.page.control.ControlType.EMAIL;
import static com.mryqr.core.common.exception.ErrorCode.ANSWER_NOT_UNIQUE_PER_APP;
import static com.mryqr.core.common.exception.ErrorCode.ANSWER_NOT_UNIQUE_PER_INSTANCE;

@Component
@RequiredArgsConstructor
public class EmailAnswerChecker extends AbstractSubmissionAnswerChecker {
    private final SubmissionRepository submissionRepository;

    @Override
    public ControlType controlType() {
        return EMAIL;
    }

    @Override
    protected Answer doCheckAnswer(Answer answer, Control control, QR qr, Page page, App app, String submissionId) {
        FEmailControl theControl = (FEmailControl) control;
        EmailAnswer theAnswer = (EmailAnswer) answer;
        EmailAnswer checkedAnswer = theControl.check(theAnswer);

        if (theControl.getUniqueType() == UNIQUE_PER_APP) {
            IndexedField indexedField = app.indexedFieldForControl(page.getId(), control.getId());
            if (submissionRepository.alreadyExistsForAnswerUnderApp(theAnswer.getEmail(),
                    indexedField,
                    app.getId(),
                    page.getId(),
                    submissionId)) {
                failAnswerValidation(ANSWER_NOT_UNIQUE_PER_APP, control, "邮箱已被占用：[" + theAnswer.getEmail() + "]。");
            }
        }

        if (theControl.getUniqueType() == UNIQUE_PER_INSTANCE) {
            IndexedField indexedField = app.indexedFieldForControl(page.getId(), control.getId());
            if (submissionRepository.alreadyExistsForAnswerUnderQr(theAnswer.getEmail(),
                    indexedField,
                    page.getId(),
                    qr.getId(),
                    submissionId)) {
                failAnswerValidation(ANSWER_NOT_UNIQUE_PER_INSTANCE, control, "邮箱已被占用：[" + theAnswer.getEmail() + "]。");
            }
        }

        return checkedAnswer;
    }
}
