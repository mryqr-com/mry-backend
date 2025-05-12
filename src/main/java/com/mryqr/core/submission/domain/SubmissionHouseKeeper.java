package com.mryqr.core.submission.domain;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.common.domain.indexedfield.IndexedValues;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.collections4.SetUtils.emptyIfNull;

@Component
@RequiredArgsConstructor
public class SubmissionHouseKeeper {

    public void perform(Submission submission, App app) {
        submission.cleanAnswers(app);
        submission.setIndexedValues(calculateIndexedValues(submission, app));
        submission.setSearchableValues(calculateSearchableValues(submission));
    }

    private IndexedValues calculateIndexedValues(Submission submission, App app) {
        if (submission.hasNoAnswers()) {
            return null;
        }

        IndexedValues indexedValues = new IndexedValues();
        submission.allAnswers().values().forEach(answer ->
                app.indexedFieldForControlOptional(submission.getPageId(), answer.getControlId())
                        .ifPresent(indexedField -> indexedValues.setFieldValue(indexedField, answer.indexedValue())));

        return indexedValues;
    }

    private Set<String> calculateSearchableValues(Submission submission) {
        if (submission.hasNoAnswers()) {
            return null;
        }

        return submission.allAnswers().values().stream()
                .map(answer -> emptyIfNull(answer.searchableValues()))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(toImmutableSet());
    }
}
