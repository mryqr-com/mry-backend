package com.mryqr.core.submission.domain.answer.multilevelselection;


import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FMultiLevelSelectionControl;
import com.mryqr.core.app.domain.page.control.MultiLevelOption;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.display.TextDisplayValue;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.MultiLevelSelectionAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@SuperBuilder
@TypeAlias("MULTI_LEVEL_SELECTION_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class MultiLevelSelectionAnswer extends Answer {
    @Valid
    @NotNull
    private MultiLevelSelection selection;

    @Override
    public void correctAndValidate() {
    }

    @Override
    public boolean isFilled() {
        return selection != null && selection.isFilled();
    }

    @Override
    public void clean(Control control) {
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return selection.indexedValues();
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
        return new MultiLevelSelectionAttributeValue(attribute, selection);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new TextDisplayValue(this.getControlId(), selection != null ? selection.displayValue() : null);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        return selection.toText();
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        FMultiLevelSelectionControl theControl = (FMultiLevelSelectionControl) control;
        String level1 = selection.getLevel1();
        String level2 = selection.getLevel2();
        String level3 = selection.getLevel3();

        if (isBlank(level1)) {
            return null;
        }

        Optional<MultiLevelOption> firstLevelOption = theControl.getOption().getOptions().stream()
                .filter(option -> Objects.equals(option.getName(), level1)).findFirst();

        if (isBlank(level2)) {
            return firstLevelOption.map(MultiLevelOption::getNumericalValue).orElse(null);
        }

        Optional<MultiLevelOption> secondLevelOption = firstLevelOption.flatMap(firstLevel -> firstLevel.getOptions().stream()
                .filter(secondLevel -> Objects.equals(secondLevel.getName(), level2)).findFirst());

        if (isBlank(level3)) {
            return secondLevelOption.map(MultiLevelOption::getNumericalValue).orElse(null);
        }

        Optional<MultiLevelOption> thirdLevelOption = secondLevelOption.flatMap(secondLevel -> secondLevel.getOptions().stream()
                .filter(thirdLevel -> Objects.equals(thirdLevel.getName(), level3)).findFirst());

        return thirdLevelOption.map(MultiLevelOption::getNumericalValue).orElse(null);
    }

    public static MultiLevelSelectionAnswer.MultiLevelSelectionAnswerBuilder<?, ?> answerBuilder(FMultiLevelSelectionControl control) {
        return MultiLevelSelectionAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
