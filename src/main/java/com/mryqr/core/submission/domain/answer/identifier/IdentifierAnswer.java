package com.mryqr.core.submission.domain.answer.identifier;


import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.TextDisplayValue;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FIdentifierControl;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.IdentifierAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.app.domain.page.control.FIdentifierControl.MAX_IDENTIFIER_LENGTH;
import static java.util.Arrays.stream;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("IDENTIFIER_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class IdentifierAnswer extends Answer {

    @Size(max = MAX_IDENTIFIER_LENGTH)
    private String content;

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return isNotBlank(content);
    }

    @Override
    public void clean(Control control) {
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return Set.of(content);
    }

    @Override
    protected Double doGetIndexedSortableValue() {
        return null;
    }

    @Override
    protected Set<String> doGetSearchableValues() {
        //以逗号或空格分开
        return isNotBlank(content) ?
                stream(content.split("[\\s,，]+")).filter(StringUtils::isNotBlank).limit(10).collect(toImmutableSet())
                : null;
    }

    @Override
    protected AttributeValue doGetAttributeValue(Attribute attribute, Control control) {
        return new IdentifierAttributeValue(attribute, content);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new TextDisplayValue(this.getControlId(), content);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        return content;
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return null;
    }

    public static IdentifierAnswer.IdentifierAnswerBuilder<?, ?> answerBuilder(FIdentifierControl control) {
        return IdentifierAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
