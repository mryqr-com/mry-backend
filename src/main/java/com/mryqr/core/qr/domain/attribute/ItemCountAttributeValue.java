package com.mryqr.core.qr.domain.attribute;

import com.mryqr.common.domain.CountedItem;
import com.mryqr.common.domain.TextOption;
import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.ItemCountDisplayValue;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FItemCountControl;
import com.mryqr.core.qr.domain.QrReferenceContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@TypeAlias("ITEM_COUNT_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class ItemCountAttributeValue extends AttributeValue {
    private String controlId;
    private List<CountedItem> items;

    public ItemCountAttributeValue(Attribute attribute, String controlId, List<CountedItem> items) {
        super(attribute);
        this.controlId = controlId;
        this.items = items;
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
    protected DisplayValue doGetDisplayValue(QrReferenceContext context) {
        return new ItemCountDisplayValue(this.getAttributeId(), items);
    }

    @Override
    public boolean isFilled() {
        return isNotEmpty(items);
    }

    @Override
    public void clean(App app) {
        app.controlByIdOptional(controlId).ifPresent(control -> {
            Set<String> allOptionIds = ((FItemCountControl) control).allOptionIds();
            this.items = items.stream().filter(item -> allOptionIds.contains(item.getOptionId())).collect(toImmutableList());
        });
    }

    @Override
    protected String doGetExportValue(Attribute attribute, QrReferenceContext context, Control refControl) {
        FItemCountControl theControl = (FItemCountControl) refControl;
        Map<String, String> optionNameMap = theControl.getOptions().stream()
                .collect(toImmutableMap(TextOption::getId, TextOption::getName));

        return items.stream().map(item -> {
            String name = optionNameMap.get(item.getOptionId());
            return isBlank(name) ? null : name + "x" + item.getNumber();
        }).filter(StringUtils::isNotBlank).collect(joining(", "));
    }
}
