package com.mryqr.core.submission.domain.answer.numberinput;


import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.NumberDisplayValue;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.DoubleAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("NUMBER_INPUT_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class NumberInputAnswer extends Answer {
    private Double number;

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return number != null;//前端有可能传空字符串，objectMapper会自动转为null值，因此判断null是ok的
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
        return number;
    }

    @Override
    protected Set<String> doGetSearchableValues() {
        return null;
    }

    @Override
    protected AttributeValue doGetAttributeValue(Attribute attribute, Control control) {
        return new DoubleAttributeValue(attribute, attribute.format(number));
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new NumberDisplayValue(this.getControlId(), number);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        FNumberInputControl theControl = (FNumberInputControl) control;
        return String.format("%." + theControl.getPrecision() + "f", number);
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        return number;
    }


    public static NumberInputAnswer.NumberInputAnswerBuilder<?, ?> answerBuilder(FNumberInputControl control) {
        return NumberInputAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
