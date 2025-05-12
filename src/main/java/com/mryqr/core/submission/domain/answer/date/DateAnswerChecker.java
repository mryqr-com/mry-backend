package com.mryqr.core.submission.domain.answer.date;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.FDateControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.answer.AbstractSubmissionAnswerChecker;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.page.control.ControlType.DATE;

@Component
@RequiredArgsConstructor
public class DateAnswerChecker extends AbstractSubmissionAnswerChecker {
    @Override
    public ControlType controlType() {
        return DATE;
    }

    @Override
    protected Answer doCheckAnswer(Answer answer, Control control, QR qr, Page page, App app, String submissionId) {
        return ((FDateControl) control).check((DateAnswer) answer);
    }
}
