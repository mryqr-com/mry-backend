package com.mryqr.core.submission.domain.answer.radio;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.FRadioControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.answer.AbstractSubmissionAnswerChecker;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.page.control.ControlType.RADIO;

@Component
@RequiredArgsConstructor
public class RadioAnswerChecker extends AbstractSubmissionAnswerChecker {
    @Override
    public ControlType controlType() {
        return RADIO;
    }

    @Override
    protected Answer doCheckAnswer(Answer answer, Control control, QR qr, Page page, App app, String submissionId) {
        return ((FRadioControl) control).check((RadioAnswer) answer);
    }
}
