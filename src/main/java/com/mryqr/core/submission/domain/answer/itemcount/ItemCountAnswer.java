package com.mryqr.core.submission.domain.answer.itemcount;


import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FItemCountControl;
import com.mryqr.core.common.domain.CountedItem;
import com.mryqr.core.common.domain.TextOption;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.display.ItemCountDisplayValue;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.collection.NoNullElement;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.ItemCountAttributeValue;
import com.mryqr.core.submission.domain.SubmissionReferenceContext;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.exception.ErrorCode.COUNTED_ITEM_ID_DUPLICATED;
import static com.mryqr.core.common.utils.Identified.isDuplicated;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@SuperBuilder
@TypeAlias("ITEM_COUNT_ANSWER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class ItemCountAnswer extends Answer {
    @Valid
    @NotNull
    @NoNullElement
    @Size(max = FItemCountControl.MAX_MAX_ITEM_SIZE)
    private List<@Valid CountedItem> items;

    @Override
    public void correctAndValidate() {
        if (isDuplicated(items)) {
            throw new MryException(COUNTED_ITEM_ID_DUPLICATED, "选项ID不能重复。", "controlId", this.getControlId());
        }
    }

    @Override
    public boolean isFilled() {
        return isNotEmpty(items);
    }

    @Override
    public void clean(Control control) {
        Set<String> allOptionIds = ((FItemCountControl) control).allOptionIds();
        this.items = items.stream().filter(item -> allOptionIds.contains(item.getOptionId())).collect(toImmutableList());
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return items.stream().map(CountedItem::getOptionId).collect(toImmutableSet());
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
        return new ItemCountAttributeValue(attribute, this.getControlId(), items);
    }

    @Override
    protected DisplayValue doGetDisplayValue(SubmissionReferenceContext context) {
        return new ItemCountDisplayValue(this.getControlId(), items);
    }

    @Override
    protected String doGetExportValue(Control control, SubmissionReferenceContext context) {
        FItemCountControl theControl = (FItemCountControl) control;
        Map<String, String> optionNameMap = theControl.getOptions().stream()
                .collect(toImmutableMap(TextOption::getId, TextOption::getName));

        return items.stream().map(item -> {
            String name = optionNameMap.get(item.getOptionId());
            return isBlank(name) ? null : name + "x" + item.getNumber();
        }).filter(StringUtils::isNotBlank).collect(joining(", "));
    }

    @Override
    protected Double doCalculateNumericalValue(Control control) {
        FItemCountControl theControl = (FItemCountControl) control;
        Map<String, TextOption> optionMap = theControl.getOptions().stream()
                .collect(toImmutableMap(TextOption::getId, identity()));

        return items.stream()
                .map(item -> {
                    TextOption textOption = optionMap.get(item.getOptionId());
                    return textOption == null ? 0.0 : textOption.getNumericalValue() * item.getNumber();
                })
                .mapToDouble(Double::doubleValue).sum();
    }

    public static ItemCountAnswer.ItemCountAnswerBuilder<?, ?> answerBuilder(FItemCountControl control) {
        return ItemCountAnswer.builder().controlId(control.getId()).controlType(control.getType());
    }

}
