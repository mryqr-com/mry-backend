package com.mryqr.core.submission.domain.answer;

import com.mryqr.common.domain.permission.Permission;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.qr.domain.QR;

import java.util.Map;
import java.util.Set;

public interface SubmissionAnswerChecker {
    ControlType controlType();

    Answer checkAnswer(Answer answer,
                       Map<String, Answer> answerMap,
                       Control control,
                       Map<String, Control> controlMap,
                       QR qr,
                       Page page,
                       App app,
                       String submissionId,
                       Set<Permission> permissions);
}
