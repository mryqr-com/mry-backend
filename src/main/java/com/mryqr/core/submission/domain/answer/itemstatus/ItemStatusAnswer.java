package com.mryqr.core.submission.domain.answer.itemstatus;


import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FItemStatusControl;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.display.TextOptionDisplayValue;
import com.mryqr.core.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.ItemStatusAttributeValue;
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
@TypeAlias("ITEM_STATUS_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class ItemStatusAnswer extends Answer {
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

        FItemStatusControl theControl = (FItemStatusControl) control;
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
        return new ItemStatusAttributeValue(attribute, this.getControlId(), optionId);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new TextOptionDisplayValue(this.getControlId(), optionId);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        FItemStatusControl theControl = (FItemStatusControl) control;
        return theControl.exportedValueFor(optionId);
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        FItemStatusControl theControl = (FItemStatusControl) control;
        return theControl.numericalValueFor(optionId);
    }

    public static ItemStatusAnswer.ItemStatusAnswerBuilder<?, ?> answerBuilder(FItemStatusControl control) {
        return ItemStatusAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
