package com.mryqr.core.qr.domain.attribute;

import com.mryqr.common.domain.Geolocation;
import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.GeolocationDisplayValue;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.qr.domain.QrReferenceContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("GEOLOCATION_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class GeolocationAttributeValue extends AttributeValue {
    private Geolocation geolocation;

    public GeolocationAttributeValue(Attribute attribute,
                                     Geolocation geolocation) {
        super(attribute);
        this.geolocation = geolocation;
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return geolocation.indexedValues();
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
        return new GeolocationDisplayValue(this.getAttributeId(), geolocation);
    }

    @Override
    public boolean isFilled() {
        return geolocation != null && geolocation.isPositioned();
    }

    @Override
    public void clean(App app) {

    }

    @Override
    protected String doGetExportValue(Attribute attribute, QrReferenceContext context, Control refControl) {
        return geolocation.toText();
    }

}
