package com.mryqr.core.submission.domain.answer.multilevelselection;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.FMultiLevelSelectionControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.answer.AbstractSubmissionAnswerChecker;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.page.control.ControlType.MULTI_LEVEL_SELECTION;

@Component
@RequiredArgsConstructor
public class MultiLevelSelectionAnswerChecker extends AbstractSubmissionAnswerChecker {
    @Override
    public ControlType controlType() {
        return MULTI_LEVEL_SELECTION;
    }

    @Override
    protected Answer doCheckAnswer(Answer answer, Control control, QR qr, Page page, App app, String submissionId) {
        return ((FMultiLevelSelectionControl) control).check((MultiLevelSelectionAnswer) answer);
    }
}
