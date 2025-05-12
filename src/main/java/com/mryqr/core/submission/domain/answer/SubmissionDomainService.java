package com.mryqr.core.submission.domain.answer;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.common.domain.permission.Permission;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.submission.domain.Submission;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_NOT_EXIST_FOR_ANSWER;
import static com.mryqr.core.common.exception.ErrorCode.PAGE_NOT_FILLABLE;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static java.util.function.Function.identity;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Component
public class SubmissionDomainService {
    private final Map<ControlType, SubmissionAnswerChecker> submissionAnswerCheckerMap;

    public SubmissionDomainService(List<SubmissionAnswerChecker> submissionAnswerCheckers) {
        this.submissionAnswerCheckerMap = submissionAnswerCheckers.stream()
                .collect(toImmutableMap(SubmissionAnswerChecker::controlType, identity()));
    }

    public Map<String, Answer> checkAnswers(Set<Answer> answers,
                                            QR qr,
                                            Page page,
                                            App app,
                                            Set<Permission> permissions) {
        return checkAnswers(answers, qr, page, app, null, permissions);
    }

    public Map<String, Answer> checkAnswers(Set<Answer> answers,
                                            QR qr,
                                            Page page,
                                            App app,
                                            String submissionId,
                                            Set<Permission> permissions) {
        String pageId = page.getId();
        List<Control> allFillableControls = page.allFillableControls();

        if (isEmpty(allFillableControls)) {
            throw new MryException(PAGE_NOT_FILLABLE, "没有可填控件。", mapOf("pageId", pageId));
        }

        Map<String, Control> fillableControlMap = allFillableControls.stream().collect(toImmutableMap(Control::getId, identity()));
        Map<String, Answer> answerMap = new HashMap<>(answers.stream().collect(toImmutableMap(Answer::getControlId, identity())));

        if (!fillableControlMap.keySet().containsAll(answerMap.keySet())) {
            throw new MryException(CONTROL_NOT_EXIST_FOR_ANSWER, "有答案对应控件不存在。", mapOf("pageId", pageId));
        }

        Map<String, Answer> checkedAnswers = new HashMap<>();
        allFillableControls.forEach(control -> {
            Answer providedAnswer = answerMap.get(control.getId());
            SubmissionAnswerChecker checker = submissionAnswerCheckerMap.get(control.getType());
            Answer checkedAnswer = checker.checkAnswer(providedAnswer,
                    answerMap,
                    control,
                    fillableControlMap,
                    qr,
                    page,
                    app,
                    submissionId,
                    permissions);

            if (checkedAnswer != null) {
                checkedAnswers.put(checkedAnswer.getControlId(), checkedAnswer);
                answerMap.put(checkedAnswer.getControlId(), checkedAnswer);//保证自动计算的control得到最新的值
            }
        });

        return Map.copyOf(checkedAnswers);
    }

    public void updateSubmission(Submission submission,
                                 App app,
                                 Page page,
                                 QR qr,
                                 Set<Answer> answers,
                                 Set<Permission> permissions,
                                 User user) {

        Map<String, Answer> checkedAnswers = checkAnswers(answers,
                qr,
                page,
                app,
                submission.getId(),
                permissions);

        Set<String> submittedControlIds = answers.stream().map(Answer::getControlId).collect(toImmutableSet());
        submission.update(submittedControlIds, checkedAnswers, user);
    }

}
