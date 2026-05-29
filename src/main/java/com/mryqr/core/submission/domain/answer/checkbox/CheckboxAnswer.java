package com.mryqr.core.submission.domain.answer.checkbox;


import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.TextOptionsDisplayValue;
import com.mryqr.common.validation.collection.NoBlankString;
import com.mryqr.common.validation.collection.NoDuplicatedString;
import com.mryqr.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FCheckboxControl;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.CheckboxAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Getter
@SuperBuilder
@TypeAlias("CHECKBOX_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class CheckboxAnswer extends Answer {

    @Valid
    @NotNull
    @NoBlankString
    @NoDuplicatedString
    @Size(max = FCheckboxControl.MAX_OPTION_SIZE)
    private List<@ShortUuid String> optionIds;

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return isNotEmpty(optionIds);
    }

    @Override
    public void clean(Control control) {
        Set<String> allOptionIds = ((FCheckboxControl) control).allOptionIds();
        this.optionIds = optionIds.stream().filter(allOptionIds::contains).collect(toImmutableList());
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return Set.copyOf(optionIds);
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
        return new CheckboxAttributeValue(attribute, this.getControlId(), optionIds);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new TextOptionsDisplayValue(this.getControlId(), optionIds);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        FCheckboxControl theControl = (FCheckboxControl) control;
        return theControl.exportedValueFor(optionIds);
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        FCheckboxControl theControl = (FCheckboxControl) control;
        return theControl.numericalValueFor(optionIds);
    }

    public static CheckboxAnswer.CheckboxAnswerBuilder<?, ?> answerBuilder(FCheckboxControl control) {
        return CheckboxAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
