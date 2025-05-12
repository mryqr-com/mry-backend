package com.mryqr.core.submission.domain.answer.personname;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.FPersonNameControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.answer.AbstractSubmissionAnswerChecker;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.page.control.ControlType.PERSON_NAME;

@Component
@RequiredArgsConstructor
public class PersonNameAnswerChecker extends AbstractSubmissionAnswerChecker {

    @Override
    public ControlType controlType() {
        return PERSON_NAME;
    }

    @Override
    protected Answer doCheckAnswer(Answer answer, Control control, QR qr, Page page, App app, String submissionId) {
        return ((FPersonNameControl) control).check((PersonNameAnswer) answer);
    }
}
