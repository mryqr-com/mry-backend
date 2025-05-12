package com.mryqr.core.submission.domain.answer.itemstatus;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.page.control.FItemStatusControl;
import com.mryqr.core.common.domain.permission.Permission;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.answer.AbstractSubmissionAnswerChecker;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.mryqr.core.app.domain.page.control.ControlType.ITEM_STATUS;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class ItemStatusAnswerChecker extends AbstractSubmissionAnswerChecker {
    @Override
    public ControlType controlType() {
        return ITEM_STATUS;
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
        FItemStatusControl theControl = (FItemStatusControl) control;
        if (theControl.shouldAutoCalculate()) {
            Optional<String> optionIdOptional = theControl.autoCalculate(answerMap, controlMap);
            return optionIdOptional.map(optionId -> ItemStatusAnswer.builder()
                            .controlId(control.getId())
                            .controlType(control.getType())
                            .optionId(optionId)
                            .build())
                    .orElse(null);
        }

        if (!isAnswerFilled(answer) && isNotBlank(theControl.getInitialOptionId())) {
            return ItemStatusAnswer.builder()
                    .controlId(control.getId())
                    .controlType(control.getType())
                    .optionId(theControl.getInitialOptionId())
                    .build();
        }

        return super.checkAnswer(answer, answerMap, control, controlMap, qr, page, app, submissionId, permissions);
    }

    @Override
    protected Answer doCheckAnswer(Answer answer, Control control, QR qr, Page page, App app, String submissionId) {
        return ((FItemStatusControl) control).check((ItemStatusAnswer) answer);
    }
}
