package com.mryqr.core.submission.domain.answer.date;


import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FDateControl;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.display.TextDisplayValue;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.LocalDateAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.core.common.utils.MryRegexConstants.DATE_PATTERN;
import static java.time.LocalDate.parse;
import static java.time.ZoneId.systemDefault;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("DATE_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class DateAnswer extends Answer {

    @Size(max = 50)
    @Pattern(regexp = DATE_PATTERN, message = "日期格式不正确。")
    private String date;

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return isNotBlank(date);
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
        return (double) parse(date).atStartOfDay(systemDefault()).toInstant().toEpochMilli();
    }

    @Override
    protected Set<String> doGetSearchableValues() {
        return null;
    }

    @Override
    protected AttributeValue doGetAttributeValue(Attribute attribute, Control control) {
        return new LocalDateAttributeValue(attribute, date);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new TextDisplayValue(this.getControlId(), date);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        return date;
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return null;
    }

    public static DateAnswer.DateAnswerBuilder<?, ?> answerBuilder(FDateControl control) {
        return DateAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
