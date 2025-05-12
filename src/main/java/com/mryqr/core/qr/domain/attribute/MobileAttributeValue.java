package com.mryqr.core.qr.domain.attribute;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.display.TextDisplayValue;
import com.mryqr.core.qr.domain.QrReferenceContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@TypeAlias("MOBILE_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class MobileAttributeValue extends AttributeValue {
    private String mobile;

    public MobileAttributeValue(Attribute attribute, String mobile) {
        super(attribute);
        this.mobile = mobile;
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return Set.of(mobile);
    }

    @Override
    protected Double doGetIndexedSortableValue() {
        return null;
    }

    @Override
    protected Set<String> doGetSearchableValues() {
        return Set.of(mobile);
    }

    @Override
    protected DisplayValue doGetDisplayValue(QrReferenceContext context) {
        return new TextDisplayValue(this.getAttributeId(), mobile);
    }

    @Override
    public boolean isFilled() {
        return isNotBlank(mobile);
    }

    @Override
    public void clean(App app) {

    }

    @Override
    protected String doGetExportValue(Attribute attribute, QrReferenceContext context, Control refControl) {
        return mobile;
    }
}
