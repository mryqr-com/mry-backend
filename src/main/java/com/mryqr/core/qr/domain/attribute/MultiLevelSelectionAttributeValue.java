package com.mryqr.core.qr.domain.attribute;

import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.TextDisplayValue;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.qr.domain.QrReferenceContext;
import com.mryqr.core.submission.domain.answer.multilevelselection.MultiLevelSelection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("MULTI_LEVEL_SELECTION_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class MultiLevelSelectionAttributeValue extends AttributeValue {
    private MultiLevelSelection selection;

    public MultiLevelSelectionAttributeValue(Attribute attribute, MultiLevelSelection selection) {
        super(attribute);
        this.selection = selection;
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
    protected DisplayValue doGetDisplayValue(QrReferenceContext context) {
        return new TextDisplayValue(this.getAttributeId(), selection.displayValue());
    }

    @Override
    public boolean isFilled() {
        return selection != null && selection.isFilled();
    }

    @Override
    public void clean(App app) {

    }

    @Override
    protected String doGetExportValue(Attribute attribute, QrReferenceContext context, Control refControl) {
        return selection.toText();
    }
}
