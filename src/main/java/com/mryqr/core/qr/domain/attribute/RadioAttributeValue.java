package com.mryqr.core.qr.domain.attribute;

import com.mryqr.common.domain.display.DisplayValue;
import com.mryqr.common.domain.display.TextOptionDisplayValue;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FRadioControl;
import com.mryqr.core.qr.domain.QrReferenceContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@TypeAlias("RADIO_VALUE")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class RadioAttributeValue extends AttributeValue {
    private String controlId;
    private String optionId;

    public RadioAttributeValue(Attribute attribute, String controlId, String optionId) {
        super(attribute);
        this.controlId = controlId;
        this.optionId = optionId;
    }

    @Override
    protected Set<String> doGetIndexedTextValues() {
        return Set.of(optionId);
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
        return new TextOptionDisplayValue(this.getAttributeId(), optionId);
    }

    @Override
    public boolean isFilled() {
        return isNotBlank(optionId);
    }

    @Override
    public void clean(App app) {
        if (isBlank(this.optionId)) {
            this.optionId = null;
            return;
        }

        app.controlByIdOptional(controlId).ifPresent(control -> {
            Set<String> allOptionIds = ((FRadioControl) control).allOptionIds();
            if (!allOptionIds.contains(optionId)) {
                this.optionId = null;
            }
        });
    }

    @Override
    protected String doGetExportValue(Attribute attribute, QrReferenceContext context, Control refControl) {
        FRadioControl theControl = (FRadioControl) refControl;
        return theControl.exportedValueFor(optionId);
    }
}
