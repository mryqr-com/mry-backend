package com.mryqr.core.submission.domain.answer.singlelinetext;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.answer.AbstractSubmissionAnswerChecker;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.page.control.ControlType.SINGLE_LINE_TEXT;

@Component
@RequiredArgsConstructor
public class SingleLineTextAnswerChecker extends AbstractSubmissionAnswerChecker {
    @Override
    public ControlType controlType() {
        return SINGLE_LINE_TEXT;
    }

    @Override
    protected Answer doCheckAnswer(Answer answer, Control control, QR qr, Page page, App app, String submissionId) {
        return ((FSingleLineTextControl) control).check((SingleLineTextAnswer) answer);
    }
}
