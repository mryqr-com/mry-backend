package com.mryqr.core.submission.domain.answer.numberranking;


import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FNumberRankingControl;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.display.NumberDisplayValue;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.IntegerAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.core.app.domain.page.control.FNumberRankingControl.MAX_RANKING_LIMIT;
import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("NUMBER_RANKING_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class NumberRankingAnswer extends Answer {
    @Min(0)
    @Max(MAX_RANKING_LIMIT)
    private int rank;

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return rank > 0;
    }

    @Override
    public void clean(Control control) {
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return null;
    }

    @Override
    protected Double doGetIndexedSortableValue() {
        return (double) rank;
    }

    @Override
    protected Set<String> doGetSearchableValues() {
        return null;
    }

    @Override
    protected AttributeValue doGetAttributeValue(Attribute attribute, Control control) {
        return new IntegerAttributeValue(attribute, rank);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new NumberDisplayValue(this.getControlId(), (double) rank);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        return String.valueOf(rank);
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return (double) rank;
    }

    public static NumberRankingAnswer.NumberRankingAnswerBuilder<?, ?> answerBuilder(FNumberRankingControl control) {
        return NumberRankingAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
