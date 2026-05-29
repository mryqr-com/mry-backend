package com.mryqr.core.submission.domain.answer.radio;


import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.TextOptionDisplayValue;
import com.mryqr.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FRadioControl;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.RadioAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("RADIO_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class RadioAnswer extends Answer {
    @ShortUuid
    private String optionId;

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return isNotBlank(optionId);
    }

    @Override
    public void clean(Control control) {
        if (isBlank(optionId)) {
            this.optionId = null;
            return;
        }

        FRadioControl theControl = (FRadioControl) control;
        if (!theControl.allOptionIds().contains(optionId)) {
            this.optionId = null;
        }
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return Set.of(optionId);
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
        return new RadioAttributeValue(attribute, this.getControlId(), optionId);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new TextOptionDisplayValue(this.getControlId(), optionId);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        FRadioControl theControl = (FRadioControl) control;
        return theControl.exportedValueFor(optionId);
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        FRadioControl theControl = (FRadioControl) control;
        return theControl.numericalValueFor(optionId);
    }

    public static RadioAnswerBuilder<?, ?> answerBuilder(FRadioControl control) {
        return RadioAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
