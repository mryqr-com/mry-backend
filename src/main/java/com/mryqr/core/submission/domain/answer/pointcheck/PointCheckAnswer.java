package com.mryqr.core.submission.domain.answer.pointcheck;


import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.PointCheckDisplayValue;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FPointCheckControl;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.PointCheckAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.mryqr.core.app.domain.page.control.FPointCheckControl.MAX_OPTION_SIZE;
import static com.mryqr.core.submission.domain.answer.pointcheck.PointCheckValue.*;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;

@Getter
@SuperBuilder
@TypeAlias("POINT_CHECK_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PointCheckAnswer extends Answer {
    @NotNull
    @Size(max = MAX_OPTION_SIZE)
    private Map<@NotBlank String, @NotNull PointCheckValue> checks;//optionId -> value

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return isNotEmpty(checks) && checks.values().stream().anyMatch(v -> v != NONE);
    }

    @Override
    public void clean(Control control) {
        FPointCheckControl theControl = (FPointCheckControl) control;
        Set<String> allOptionIds = theControl.allOptionIds();
        this.checks = checks.entrySet().stream().filter(entry -> allOptionIds.contains(entry.getKey()))
                .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return isPassed() ? Set.of(YES.name()) : Set.of(NO.name());
    }

    @Override
    protected Double doGetIndexedSortableValue() {
        return null;
    }

    @Override
    protected Set<String> doGetSearchableValues() {
        return null;
    }

    @Override
    protected AttributeValue doGetAttributeValue(Attribute attribute, Control control) {
        return new PointCheckAttributeValue(attribute, isPassed());
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new PointCheckDisplayValue(this.getControlId(), isPassed());
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        return isPassed() ? "正常" : "异常";
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return null;
    }

    public boolean isPassed() {
        return checks.values().stream().allMatch(value -> value == YES);
    }

    public static PointCheckAnswer.PointCheckAnswerBuilder<?, ?> answerBuilder(FPointCheckControl control) {
        return PointCheckAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
