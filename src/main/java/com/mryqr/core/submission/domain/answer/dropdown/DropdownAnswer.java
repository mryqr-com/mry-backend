package com.mryqr.core.submission.domain.answer.dropdown;


import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FDropdownControl;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.display.TextOptionsDisplayValue;
import com.mryqr.core.common.validation.collection.NoBlankString;
import com.mryqr.core.common.validation.collection.NoDuplicatedString;
import com.mryqr.core.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.DropdownAttributeValue;
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
@TypeAlias("DROPDOWN_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class DropdownAnswer extends Answer {
    @Valid
    @NotNull
    @NoBlankString
    @NoDuplicatedString
    @Size(max = FDropdownControl.MAX_OPTION_SIZE)
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
        Set<String> allOptionIds = ((FDropdownControl) control).allOptionIds();
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
        return new DropdownAttributeValue(attribute, this.getControlId(), optionIds);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new TextOptionsDisplayValue(this.getControlId(), optionIds);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        FDropdownControl theControl = (FDropdownControl) control;
        return theControl.exportedValueFor(optionIds);
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        FDropdownControl theControl = (FDropdownControl) control;
        return theControl.numericalValueFor(optionIds);
    }

    public static DropdownAnswer.DropdownAnswerBuilder<?, ?> answerBuilder(FDropdownControl control) {
        return DropdownAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
