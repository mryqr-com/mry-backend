package com.mryqr.core.submission.domain;

import com.mryqr.common.domain.indexedfield.IndexedValues;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.qr.domain.QR;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.collections4.SetUtils.emptyIfNull;

@Component
@RequiredArgsConstructor
public class SubmissionHouseKeeper {

    public void perform(Submission submission, QR qr, App app) {
        submission.cleanAnswers(app);
        submission.setIndexedValues(calculateIndexedValues(submission, app));
        submission.setSearchableValues(calculateSearchableValues(submission, qr));
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

    private Set<String> calculateSearchableValues(Submission submission, QR qr) {
        if (submission.hasNoAnswers()) {
            return null;
        }

        Set<String> answerSearchableValues = submission.allAnswers().values().stream()
                .map(answer -> emptyIfNull(answer.searchableValues()))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(toImmutableSet());

        return Stream.of(answerSearchableValues, qr.getText(), Set.of(qr.getName()))
                .flatMap(Set::stream)
                .collect(toImmutableSet());
    }
}
