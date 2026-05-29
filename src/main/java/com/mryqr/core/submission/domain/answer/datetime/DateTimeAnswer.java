package com.mryqr.core.submission.domain.answer.datetime;


import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.TextDisplayValue;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FDateTimeControl;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.TimestampAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.time.Instant;
import java.util.Set;

import static com.mryqr.common.utils.MryConstants.MRY_DATE_TIME_FORMATTER;
import static com.mryqr.common.utils.MryRegexConstants.DATE_PATTERN;
import static com.mryqr.common.utils.MryRegexConstants.TIME_PATTERN;
import static java.time.LocalDateTime.parse;
import static java.time.ZoneId.systemDefault;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("DATE_TIME_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class DateTimeAnswer extends Answer {

    @Pattern(regexp = DATE_PATTERN, message = "日期格式不正确。")
    private String date;

    @Pattern(regexp = TIME_PATTERN, message = "时间格式不正确。")
    private String time;

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return isNotBlank(date) && isNotBlank(time);
    }

    @Override
    public void clean(Control control) {
        if (isBlank(date)) {
            this.date = null;
            this.time = null;
            return;
        }

        if (isBlank(time)) {
            time = "00:00";
        }
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return null;
    }

    @Override
    protected Double doGetIndexedSortableValue() {
        return (double) this.toInstant().toEpochMilli();
    }


    @Override
    protected Set<String> doGetSearchableValues() {
        return null;
    }

    @Override
    protected AttributeValue doGetAttributeValue(Attribute attribute, Control control) {
        return new TimestampAttributeValue(attribute, this.toInstant());
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new TextDisplayValue(this.getControlId(), this.toDateTimeString());
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        return this.toDateTimeString();
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return null;
    }

    public static DateTimeAnswer.DateTimeAnswerBuilder<?, ?> answerBuilder(FDateTimeControl control) {
        return DateTimeAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

    public Instant toInstant() {
        return parse(this.toDateTimeString(), MRY_DATE_TIME_FORMATTER).atZone(systemDefault()).toInstant();
    }

    public String toDateTimeString() {
        return date + " " + time;
    }

}
