package com.mryqr.core.submission.domain.answer.numberinput;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.common.domain.permission.Permission;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.answer.AbstractSubmissionAnswerChecker;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.mryqr.core.app.domain.page.control.ControlType.NUMBER_INPUT;

@Component
@RequiredArgsConstructor
public class NumberInputAnswerChecker extends AbstractSubmissionAnswerChecker {
    @Override
    public ControlType controlType() {
        return NUMBER_INPUT;
    }

    @Override
    public Answer checkAnswer(Answer answer,
                              Map<String, Answer> answerMap,
                              Control control,
                              Map<String, Control> controlMap,
                              QR qr,
                              Page page,
                              App app,
                              String submissionId,
                              Set<Permission> permissions) {
        FNumberInputControl theControl = (FNumberInputControl) control;

        if (theControl.shouldAutoCalculate()) {//autoCalculate无论控件是否对提交者可见，均会运行
            Optional<Double> optionIdOptional = theControl.autoCalculate(answerMap, controlMap);
            return optionIdOptional.map(value -> NumberInputAnswer.builder()
                            .controlId(control.getId())
                            .controlType(control.getType())
                            .number(value)
                            .build())
                    .orElse(null);
        }

        return super.checkAnswer(answer, answerMap, control, controlMap, qr, page, app, submissionId, permissions);
    }

    @Override
    protected Answer doCheckAnswer(Answer answer, Control control, QR qr, Page page, App app, String submissionId) {
        return ((FNumberInputControl) control).check((NumberInputAnswer) answer);
    }
}
