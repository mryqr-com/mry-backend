package com.mryqr.core.submission.query.autocalculate;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FItemStatusControl;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

@Component
@RequiredArgsConstructor
public class AutoCalculateQueryService {
    private final AppRepository appRepository;
    private final MryRateLimiter mryRateLimiter;

    public Double calculateForNumberInput(AutoCalculateQuery queryCommand, User user) {
        App app = appRepository.cachedById(queryCommand.getAppId());
        Page page = app.pageById(queryCommand.getPageId());

        if (app.requireLogin() || page.requireLogin()) {
            user.checkIsLoggedInFor(app.getTenantId());
        }

        mryRateLimiter.applyFor(app.getTenantId(), "Submission:AutoCalculateNumberInput", 50);

        FNumberInputControl control = (FNumberInputControl) app.controlById(queryCommand.getControlId());
        if (!control.shouldAutoCalculate()) {
            return null;
        }

        Set<String> dependentControlIds = control.autoCalculateDependentControlIds();

        Map<String, Control> dependentControlMap = app.allControls().stream()
                .filter(e -> dependentControlIds.contains(e.getId()))
                .collect(toImmutableMap(Control::getId, identity()));

        Map<String, Answer> dependentFilledAnswerMap = queryCommand.getAnswers().stream()
                .filter(e -> dependentControlIds.contains(e.getControlId()))
                .filter(Answer::isFilled)
                .collect(toImmutableMap(Answer::getControlId, identity()));

        if (dependentFilledAnswerMap.size() != dependentControlMap.size()) {
            return null;//未提供所有的answer
        }

        return control.autoCalculate(dependentFilledAnswerMap, dependentControlMap).orElse(null);
    }

    public String calculateForItemStatus(AutoCalculateQuery queryCommand, User user) {
        App app = appRepository.cachedById(queryCommand.getAppId());
        Page page = app.pageById(queryCommand.getPageId());

        if (app.requireLogin() || page.requireLogin()) {
            user.checkIsLoggedInFor(app.getTenantId());
        }

        mryRateLimiter.applyFor(app.getTenantId(), "Submission:AutoCalculateItemStatus", 50);

        FItemStatusControl control = (FItemStatusControl) app.controlById(queryCommand.getControlId());
        if (!control.shouldAutoCalculate()) {
            return null;
        }

        Set<String> dependentControlIds = control.autoCalculateDependentControlIds();

        Map<String, Control> dependentControlMap = app.allControls().stream()
                .filter(e -> dependentControlIds.contains(e.getId()))
                .collect(toImmutableMap(Control::getId, identity()));

        Map<String, Answer> dependentFilledAnswerMap = queryCommand.getAnswers().stream()
                .filter(e -> dependentControlIds.contains(e.getControlId()))
                .filter(Answer::isFilled)
                .collect(toImmutableMap(Answer::getControlId, identity()));

        if (dependentFilledAnswerMap.size() != dependentControlMap.size()) {
            return null;//未提供所有的answer
        }

        return control.autoCalculate(dependentFilledAnswerMap, dependentControlMap).orElse(null);
    }
}
