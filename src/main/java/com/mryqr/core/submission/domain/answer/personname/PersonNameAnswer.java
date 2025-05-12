package com.mryqr.core.submission.domain.answer.personname;


import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FPersonNameControl;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.display.TextDisplayValue;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.IdentifierAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("PERSON_NAME_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PersonNameAnswer extends Answer {
    public static final int MAX_PERSON_NAME_LENGTH = 50;

    @Size(max = MAX_PERSON_NAME_LENGTH)
    private String name;

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return isNotBlank(name);
    }

    @Override
    public void clean(Control control) {
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return Set.of(name);
    }

    @Override
    protected Double doGetIndexedSortableValue() {
        return null;
    }

    @Override
    protected Set<String> doGetSearchableValues() {
        return Set.of(name);
    }

    @Override
    protected AttributeValue doGetAttributeValue(Attribute attribute, Control control) {
        return new IdentifierAttributeValue(attribute, name);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new TextDisplayValue(this.getControlId(), name);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        return name;
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return null;
    }

    public static PersonNameAnswer.PersonNameAnswerBuilder<?, ?> answerBuilder(FPersonNameControl control) {
        return PersonNameAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
