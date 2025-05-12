package com.mryqr.core.qr.domain.attribute;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FDropdownControl;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.display.TextOptionsDisplayValue;
import com.mryqr.core.qr.domain.QrReferenceContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Getter
@TypeAlias("DROPDOWN_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class DropdownAttributeValue extends AttributeValue {
    private String controlId;
    private List<String> optionIds;

    public DropdownAttributeValue(Attribute attribute, String controlId, List<String> optionIds) {
        super(attribute);
        this.controlId = controlId;
        this.optionIds = optionIds;
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
    protected DisplayValue doGetDisplayValue(QrReferenceContext context) {
        return new TextOptionsDisplayValue(this.getAttributeId(), optionIds);
    }

    @Override
    public boolean isFilled() {
        return isNotEmpty(optionIds);
    }

    @Override
    public void clean(App app) {
        app.controlByIdOptional(controlId).ifPresent(control -> {
            Set<String> allOptionIds = ((FDropdownControl) control).allOptionIds();
            this.optionIds = optionIds.stream().filter(allOptionIds::contains).collect(toImmutableList());
        });
    }

    @Override
    protected String doGetExportValue(Attribute attribute, QrReferenceContext context, Control refControl) {
        FDropdownControl theControl = (FDropdownControl) refControl;
        return theControl.exportedValueFor(optionIds);
    }
}
