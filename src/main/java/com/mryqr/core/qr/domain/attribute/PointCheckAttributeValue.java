package com.mryqr.core.qr.domain.attribute;

import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.PointCheckDisplayValue;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.qr.domain.QrReferenceContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.core.submission.domain.answer.pointcheck.PointCheckValue.NO;
import static com.mryqr.core.submission.domain.answer.pointcheck.PointCheckValue.YES;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("POINT_CHECK_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PointCheckAttributeValue extends AttributeValue {
    private boolean pass;

    public PointCheckAttributeValue(Attribute attribute, boolean pass) {
        super(attribute);
        this.pass = pass;
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return Set.of(pass ? YES.name() : NO.name());
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
        return new PointCheckDisplayValue(this.getAttributeId(), pass);
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
        return pass ? "正常" : "异常";
    }
}
