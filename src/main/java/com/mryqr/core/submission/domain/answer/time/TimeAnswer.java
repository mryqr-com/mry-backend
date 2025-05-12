package com.mryqr.core.submission.domain.answer.time;


import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FTimeControl;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.display.TextDisplayValue;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.LocalTimeAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.core.common.utils.MryRegexConstants.TIME_PATTERN;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("TIME_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class TimeAnswer extends Answer {
    @Pattern(regexp = TIME_PATTERN, message = "时间格式不正确。")
    private String time;

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return isNotBlank(time);
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
        return null;
    }

    @Override
    protected Set<String> doGetSearchableValues() {
        return null;
    }

    @Override
    protected AttributeValue doGetAttributeValue(Attribute attribute, Control control) {
        return new LocalTimeAttributeValue(attribute, time);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new TextDisplayValue(this.getControlId(), time);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        return time;
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return null;
    }

    public static TimeAnswer.TimeAnswerBuilder<?, ?> answerBuilder(FTimeControl control) {
        return TimeAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
