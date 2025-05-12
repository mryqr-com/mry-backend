package com.mryqr.core.qr.domain.attribute;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.common.domain.Address;
import com.mryqr.core.common.domain.display.AddressDisplayValue;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.qr.domain.QrReferenceContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("ADDRESS_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class AddressAttributeValue extends AttributeValue {
    private Address address;

    public AddressAttributeValue(Attribute attribute, Address address) {
        super(attribute);
        this.address = address;
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return address.indexedValues();
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
        return new AddressDisplayValue(this.getAttributeId(), address);
    }

    @Override
    public boolean isFilled() {
        return address != null && address.isFilled();
    }

    @Override
    public void clean(App app) {

    }

    @Override
    protected String doGetExportValue(Attribute attribute, QrReferenceContext context, Control refControl) {
        return address.toText();
    }
}
