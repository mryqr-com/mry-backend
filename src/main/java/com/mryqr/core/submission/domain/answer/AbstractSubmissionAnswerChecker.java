package com.mryqr.core.submission.domain.answer;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.common.domain.permission.Permission;
import com.mryqr.core.common.exception.ErrorCode;
import com.mryqr.core.qr.domain.QR;

import java.util.Map;
import java.util.Set;

import static com.mryqr.core.common.exception.ErrorCode.MANDATORY_ANSWER_REQUIRED;
import static com.mryqr.core.common.exception.MryException.accessDeniedException;

public abstract class AbstractSubmissionAnswerChecker implements SubmissionAnswerChecker {

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
        boolean hasPermission = permissions.contains(control.requiredPermission());

        //提供了answer但是权限不够(无论是否有填值)，则报错
        if (isAnswerProvided(answer) && !hasPermission) {
            throw accessDeniedException("无权填写控件项:[" + control.getName() + "]。");
        }

        if (isAnswerFilled(answer)) {
            return doCheckAnswer(answer, control, qr, page, app, submissionId);
        }

        if (control.isMandatory()) {
            failAnswerValidation(MANDATORY_ANSWER_REQUIRED, control, "未回答必填项:[" + control.getName() + "]。");
        }

        return null;
    }

    protected final boolean isAnswerFilled(Answer answer) {
        return isAnswerProvided(answer) && answer.isFilled();//answer有填值
    }

    protected final boolean isAnswerProvided(Answer answer) {
        return answer != null;//只保证有提供answer，但是不管是否有填值
    }

    protected final void failAnswerValidation(ErrorCode errorCode, Control control, String message) {
        control.failAnswerValidation(errorCode, message, message);
    }

    //对填值的answer进行检查
    protected abstract Answer doCheckAnswer(Answer answer, Control control, QR qr, Page page, App app, String submissionId);
}
