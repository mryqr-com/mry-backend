package com.mryqr.core.qr.domain.attribute;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.common.domain.display.TimestampDisplayValue;
import com.mryqr.core.qr.domain.QrReferenceContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.time.Instant;
import java.util.Set;

import static com.mryqr.core.common.utils.MryConstants.MRY_DATE_TIME_FORMATTER;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("TIMESTAMP_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class TimestampAttributeValue extends AttributeValue {
    private Instant timestamp;

    public TimestampAttributeValue(Attribute attribute, Instant timestamp) {
        super(attribute);
        this.timestamp = timestamp;
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return null;
    }

    @Override
    protected Double doGetIndexedSortableValue() {
        return (double) timestamp.toEpochMilli();
    }

    @Override
    protected Set<String> doGetSearchableValues() {
        return null;
    }

    @Override
    protected DisplayValue doGetDisplayValue(QrReferenceContext context) {
        return new TimestampDisplayValue(this.getAttributeId(), timestamp);
    }

    @Override
    public boolean isFilled() {
        return timestamp != null;
    }

    @Override
    public void clean(App app) {

    }

    @Override
    protected String doGetExportValue(Attribute attribute, QrReferenceContext context, Control refControl) {
        return MRY_DATE_TIME_FORMATTER.format(timestamp);
    }
}
