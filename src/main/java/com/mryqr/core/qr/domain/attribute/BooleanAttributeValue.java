package com.mryqr.core.qr.domain.attribute;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.common.domain.display.BooleanDisplayValue;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.qr.domain.QrReferenceContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("BOOLEAN_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class BooleanAttributeValue extends AttributeValue {
    public static final String TRUE = "TRUE";
    public static final String FALSE = "FALSE";

    private boolean yes;

    public BooleanAttributeValue(Attribute attribute, boolean yes) {
        super(attribute);
        this.yes = yes;
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return Set.of(yes ? TRUE : FALSE);
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
        return new BooleanDisplayValue(this.getAttributeId(), yes);
    }

    @Override
    public boolean isFilled() {
        return true;
    }

    @Override
    public void clean(App app) {

    }

    @Override
    protected String doGetExportValue(Attribute attribute, QrReferenceContext context, Control refControl) {
        return yes ? "是" : "否";
    }
}
